package com.mywordsmyway.model

import com.mywordsmyway.data.model.BorromeanExtractionResult
import com.mywordsmyway.data.model.BorromeanWordCandidate
import com.mywordsmyway.data.model.NounExtractionResult
import com.mywordsmyway.data.model.SafetyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.InvocationTargetException

class GemmaModelService(
    private val settingsRepository: ModelSettingsRepository,
    private val fallback: ModelService = MockModelService(),
) : ModelService {
    override suspend fun transcribe(audioPath: String): String = withContext(Dispatchers.Default) {
        val settings = settingsRepository.settings.first()
        if (!settings.isLoadable) {
            return@withContext ""
        }
        runCatching {
            val prompt = "Transcribe this local audio file exactly and return only text: $audioPath"
            generate(prompt).text
        }.getOrElse { "" }
    }

    override suspend fun safetyCheck(text: String): SafetyResult =
        fallback.safetyCheck(text)

    override suspend fun extractNouns(sourceText: String, existingNouns: List<String>): NounExtractionResult =
        fallback.extractNouns(sourceText, existingNouns)

    override suspend fun extractBorromeanWords(sourceText: String, existingWords: List<String>): BorromeanExtractionResult = withContext(Dispatchers.Default) {
        val settings = settingsRepository.settings.first()
        if (!settings.isLoadable) {
            val reason = if (settings.isConfigured) {
                "Gemma model file is not loadable: ${settings.gemmaModelPath}"
            } else {
                "Gemma model path is not configured."
            }
            return@withContext BorromeanExtractionResult(candidateWords = emptyList(), diagnostic = reason)
        }
        runCatching {
            val prompt = buildBorromeanPrompt(sourceText, existingWords)
            val generated = generate(prompt)
            runCatching {
                parseBorromeanJson(generated.text, generated.backend)
            }.getOrElse { throwable ->
                BorromeanExtractionResult(
                    candidateWords = emptyList(),
                    diagnostic = "Gemma response parsing failed: ${throwable.describeForUser()}",
                    backend = generated.backend,
                )
            }.withDiagnostic(generated.diagnostic)
        }.getOrElse { throwable ->
            BorromeanExtractionResult(
                candidateWords = emptyList(),
                diagnostic = "Gemma extraction failed: ${throwable.describeForUser()}",
            )
        }
    }

    override suspend fun modelStatus(): String {
        val settings = settingsRepository.settings.first()
        return when {
            !settings.isConfigured -> "Gemma model path is not configured; Words extraction is disabled."
            !settings.isLoadable -> "Gemma model file is not loadable; Words extraction is disabled."
            !settings.isExpectedGemmaModel -> "Gemma model is loadable, but it is not the expected Gemma 4 E2B file. Download or import gemma-4-E2B-it.litertlm."
            else -> "Gemma model configured: ${settings.gemmaModelPath}"
        }
    }

    private suspend fun generate(prompt: String): GeneratedText {
        val settings = settingsRepository.settings.first()
        require(settings.isLoadable) { "Gemma model file is not loadable." }
        // LiteRT-LM APIs are still evolving. Keep the call isolated so the app can
        // fall back cleanly if the local model/runtime is not available.
        return LiteRtLmReflection.generate(settings.gemmaModelPath, prompt)
    }

    private fun buildBorromeanPrompt(sourceText: String, existingWords: List<String>): String =
        """
        You are not a therapist, not a psychoanalyst, and not an advisor.

        From the transcript, extract important words that may be connected to the user's early experience with their mother. A Borromean knot is generated because the person has something that lets you have familiar feelings that you had when you were little with your mom. Object a is in the central hole. The Borromean knot is organized around this hole. You can fill it with imaginary words, but it will never be that object, because object a is the missing place itself. These words are important because they come from our mom, also because we love our mom.

        Focus on:
        1. Important words connected to scenes where the child was with mother;
        2. Important words connected to scenes where mother left, disappeared, was absent, or became unavailable;
        3. Important words connected to what the child imagined mother wanted;
        4. Important words connected to what the child thought they needed to become, have, or do so the mother would stay.
        5. Object a fragments only contain words that trigger users to think about their mom when they were little.
        6. Personally important words are the words from the 1-4, and users say it with lots of emotional feelings, because it connects to their mom.
        7. What I truly want - the words that the user's mom told or showed them and the users think they want to get it through effort and determination.

        Do not interpret the user.
        Do not diagnose.
        Do not say what the user should do.

        Avoid duplicates from this list: ${existingWords.joinToString(", ")}

        Return JSON only:
        {
          "candidate_nouns": [
            {
              "Important word": "...",
              "type": "Object a fragments | Personally important words | What I truly want",
              "scene_type": "with_mom | mom_left | what_mom_wanted | unclear",
              "evidence_span": "...",
              "confidence": "low | medium | high",
              "suggestion": "add | update_existing | ignore"
            }
          ]
        }

        Return at most 12 candidate_nouns.

        Transcript:
        ${sourceText.take(MAX_EXTRACTION_SOURCE_CHARS)}
        """.trimIndent()

    private fun parseBorromeanJson(response: String, backend: String): BorromeanExtractionResult {
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        require(jsonStart >= 0 && jsonEnd > jsonStart) { "Gemma did not return JSON." }
        val root = JSONObject(response.substring(jsonStart, jsonEnd + 1))
        val candidates = root.optJSONArray("candidate_nouns").toBorromeanCandidates()
        return BorromeanExtractionResult(candidateWords = candidates, backend = backend)
    }
}

private object LiteRtLmReflection {
    fun generate(modelPath: String, prompt: String): GeneratedText {
        require(File(modelPath).isFile) { "Model file does not exist." }
        val engineConfigClass = Class.forName("com.google.ai.edge.litertlm.EngineConfig")
        val backendClass = Class.forName("com.google.ai.edge.litertlm.Backend")
        val cpuBackend = backendClass.declaredClasses.firstOrNull { it.simpleName == "CPU" }
            ?.constructCpuBackend(numThreads = 2)
            ?: error("LiteRT-LM CPU backend is unavailable.")
        val constructor = engineConfigClass.constructors.firstOrNull { constructor ->
            constructor.parameterTypes.firstOrNull() == String::class.java
        }
            ?: error("LiteRT-LM EngineConfig API is unavailable.")
        val backendOptions = listOf("CPU (2 threads)" to cpuBackend)
        val errors = mutableListOf<String>()
        backendOptions.forEach { (backendName, backend) ->
            runCatching {
                val result = generateWithBackend(
                    modelPath = modelPath,
                    prompt = prompt,
                    constructor = constructor,
                    engineConfigClass = engineConfigClass,
                    backend = backend,
                    backendName = backendName,
                )
                val diagnostic = if (errors.isEmpty()) {
                    ""
                } else {
                    "Gemma used $backendName after fallback. ${errors.joinToString("; ")}"
                }
                return result.copy(diagnostic = diagnostic)
            }.onFailure { throwable ->
                errors += "$backendName failed: ${throwable.describeForUser()}"
            }
        }
        error(errors.joinToString("; ").ifBlank { "LiteRT-LM generation failed." })
    }

    private fun generateWithBackend(
        modelPath: String,
        prompt: String,
        constructor: java.lang.reflect.Constructor<*>,
        engineConfigClass: Class<*>,
        backend: Any,
        backendName: String,
    ): GeneratedText {
        var engine: Any? = null
        var conversation: Any? = null
        try {
            val config = when (constructor.parameterCount) {
                1 -> constructor.newInstance(modelPath)
                2 -> constructor.newInstance(modelPath, backend)
                7 -> constructor.newInstance(modelPath, backend, null, null, MAX_NUM_TOKENS, null, null)
                else -> error("Unsupported LiteRT-LM EngineConfig constructor with ${constructor.parameterCount} parameters.")
            }
            val engineClass = Class.forName("com.google.ai.edge.litertlm.Engine")
            engine = engineClass.getConstructor(engineConfigClass).newInstance(config)
            engineClass.methods.firstOrNull { it.name == "initialize" }?.invoke(engine)
            val createConversation = engineClass.methods
                .filter { it.name == "createConversation" }
                .minByOrNull { it.parameterCount }
                ?: error("LiteRT-LM createConversation API is unavailable.")
            val conversationArgs = createConversation.parameterTypes.map { parameterType ->
                when (parameterType.name) {
                    "com.google.ai.edge.litertlm.ConversationConfig" -> {
                        parameterType.constructKotlinDefaultInstance()
                            ?: error("LiteRT-LM ConversationConfig default constructor is unavailable.")
                    }
                    else -> null
                }
            }.toTypedArray()
            conversation = createConversation.invoke(engine, *conversationArgs)
            val send = conversation.javaClass.methods
                .filter { it.name == "sendMessage" || it.name == "sendMessageSync" }
                .firstOrNull { method ->
                    method.parameterCount == 1 && method.parameterTypes.firstOrNull()?.isAssignableFrom(String::class.java) == true
                }
                ?: conversation.javaClass.methods.firstOrNull { method ->
                    method.name == "sendMessage" &&
                        method.parameterCount == 2 &&
                        method.parameterTypes.firstOrNull()?.isAssignableFrom(String::class.java) == true &&
                        Map::class.java.isAssignableFrom(method.parameterTypes[1])
                }
                ?: error("LiteRT-LM send API is unavailable.")
            val response = when (send.parameterCount) {
                1 -> send.invoke(conversation, prompt)
                2 -> send.invoke(conversation, prompt, emptyMap<String, Any>())
                else -> error("Unsupported LiteRT-LM send API with ${send.parameterCount} parameters.")
            }
            return GeneratedText(text = response?.toString().orEmpty(), backend = backendName)
        } finally {
            conversation.closeLiteRtResource()
            engine.closeLiteRtResource()
            System.gc()
        }
    }

    private fun Any?.closeLiteRtResource() {
        if (this == null) return
        if (this is AutoCloseable) {
            runCatching { close() }
            return
        }
        val closeMethod = javaClass.methods.firstOrNull { method ->
            method.parameterCount == 0 && method.name in RESOURCE_CLOSE_METHOD_NAMES
        }
        runCatching { closeMethod?.invoke(this) }
    }

    private fun Class<*>.constructKotlinDefaultInstance(): Any? {
        getDeclaredConstructorOrNull()?.let { constructor ->
            constructor.isAccessible = true
            return constructor.newInstance()
        }
        constructors.firstOrNull { it.parameterCount == 1 && !it.parameterTypes.first().isPrimitive }?.let { constructor ->
            return constructor.newInstance(null)
        }
        constructors.firstOrNull { constructor ->
            constructor.parameterTypes.lastOrNull()?.name == "kotlin.jvm.internal.DefaultConstructorMarker"
        }?.let { constructor ->
            val args = Array<Any?>(constructor.parameterCount) { null }
            args[constructor.parameterCount - 2] = 1
            return constructor.newInstance(*args)
        }
        return null
    }

    private fun Class<*>.getDeclaredConstructorOrNull() =
        runCatching { getDeclaredConstructor() }.getOrNull()

    private fun Class<*>.constructCpuBackend(numThreads: Int): Any? {
        constructors.firstOrNull { constructor ->
            constructor.parameterCount == 1 && constructor.parameterTypes.first().name == "java.lang.Integer"
        }?.let { return it.newInstance(numThreads) }
        constructors.firstOrNull { constructor ->
            constructor.parameterCount == 1 && constructor.parameterTypes.first() == Int::class.javaPrimitiveType
        }?.let { return it.newInstance(numThreads) }
        return constructKotlinDefaultInstance()
    }

    private val RESOURCE_CLOSE_METHOD_NAMES = setOf("close", "release", "destroy", "dispose", "shutdown")
}

private const val MAX_EXTRACTION_SOURCE_CHARS = 4000
private const val MAX_NUM_TOKENS = 2048

private data class GeneratedText(
    val text: String,
    val backend: String,
    val diagnostic: String = "",
)

private fun BorromeanExtractionResult.withDiagnostic(extraDiagnostic: String): BorromeanExtractionResult {
    if (extraDiagnostic.isBlank()) return this
    val combined = listOf(diagnostic, extraDiagnostic)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return copy(diagnostic = combined)
}

private fun Throwable.describeForUser(): String {
    val root = unwrapReflectiveCause()
    val message = root.message?.takeIf { it.isNotBlank() }
    return if (message == null) {
        root::class.java.simpleName
    } else {
        "${root::class.java.simpleName}: $message"
    }
}

private tailrec fun Throwable.unwrapReflectiveCause(): Throwable =
    when (this) {
        is InvocationTargetException -> targetException?.unwrapReflectiveCause() ?: this
        else -> cause?.takeIf { this is ExceptionInInitializerError || this::class.java.name.startsWith("java.lang.reflect") }
            ?.unwrapReflectiveCause()
            ?: this
    }

private fun JSONArray?.toBorromeanCandidates(): List<BorromeanWordCandidate> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            if (item.optString("suggestion").trim().lowercase() == "ignore") continue
            val importantWord = item.optString("Important word").trim()
                .ifBlank { item.optString("important_word").trim() }
                .ifBlank { item.optString("word").trim() }
                .ifBlank { item.optString("noun").trim() }
            if (importantWord.isBlank()) continue
            val register = registerForExtractedType(item.optString("type")) ?: continue
            add(
                BorromeanWordCandidate(
                    text = importantWord,
                    registerType = register,
                    objectPartType = null,
                    emotionalWeight = item.optInt("emotional_weight", 50),
                    importanceWeight = item.optInt("importance_weight", 50),
                    desireWeight = item.optInt("desire_weight", 50),
                    evidence = item.optString("evidence_span"),
                    confidence = item.optString("confidence", "low"),
                ),
            )
        }
    }
}

private fun registerForExtractedType(type: String): String? =
    when (type.trim().lowercase()) {
        "object a fragments" -> "object_a"
        "personally important words" -> "affect"
        "what i truly want" -> "desire"
        else -> null
    }
