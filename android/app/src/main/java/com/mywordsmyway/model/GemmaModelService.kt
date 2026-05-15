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

class GemmaModelService(
    private val settingsRepository: ModelSettingsRepository,
    private val fallback: ModelService = MockModelService(),
) : ModelService {
    override suspend fun transcribe(audioPath: String): String = withContext(Dispatchers.Default) {
        runCatching {
            val prompt = "Transcribe this local audio file exactly and return only text: $audioPath"
            generate(prompt)
        }.getOrElse { fallback.transcribe(audioPath) }
    }

    override suspend fun safetyCheck(text: String): SafetyResult =
        fallback.safetyCheck(text)

    override suspend fun extractNouns(sourceText: String, existingNouns: List<String>): NounExtractionResult =
        fallback.extractNouns(sourceText, existingNouns)

    override suspend fun extractBorromeanWords(sourceText: String, existingWords: List<String>): BorromeanExtractionResult = withContext(Dispatchers.Default) {
        runCatching {
            val prompt = buildBorromeanPrompt(sourceText, existingWords)
            parseBorromeanJson(generate(prompt))
        }.getOrElse {
            fallback.extractBorromeanWords(sourceText, existingWords)
        }
    }

    override suspend fun modelStatus(): String {
        val settings = settingsRepository.settings.first()
        return when {
            !settings.isConfigured -> "Gemma model path not configured; using mock extraction."
            !settings.isLoadable -> "Gemma model file is not loadable; using mock extraction."
            else -> "Gemma model configured: ${settings.gemmaModelPath}"
        }
    }

    private suspend fun generate(prompt: String): String {
        val settings = settingsRepository.settings.first()
        require(settings.isLoadable) { "Gemma model file is not loadable." }
        // LiteRT-LM APIs are still evolving. Keep the call isolated so the app can
        // fall back cleanly if the local model/runtime is not available.
        return LiteRtLmReflection.generate(settings.gemmaModelPath, prompt)
    }

    private fun buildBorromeanPrompt(sourceText: String, existingWords: List<String>): String =
        """
        You are extracting words for a private Lacanian notes app.
        Extract JSON only with this schema:
        {"candidate_words":[{"text":"","register_type":"object_a|affect|desire","object_part_type":"gaze|voice|breast|excrement|other|null","emotional_weight":0,"importance_weight":0,"desire_weight":0,"evidence":"","confidence":"low|medium|high"}]}

        Meanings:
        - object_a: words that remind the user of their mom or early familiar feelings with mom.
        - affect: personally important words from mom.
        - desire: what the user truly wants, extracted from mom's words.

        Avoid duplicates from this list: ${existingWords.joinToString(", ")}

        Source:
        ${sourceText.take(12000)}
        """.trimIndent()

    private fun parseBorromeanJson(response: String): BorromeanExtractionResult {
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}')
        require(jsonStart >= 0 && jsonEnd > jsonStart) { "Gemma did not return JSON." }
        val root = JSONObject(response.substring(jsonStart, jsonEnd + 1))
        val candidates = root.optJSONArray("candidate_words").toBorromeanCandidates()
        return BorromeanExtractionResult(candidateWords = candidates)
    }
}

private object LiteRtLmReflection {
    fun generate(modelPath: String, prompt: String): String {
        require(File(modelPath).isFile) { "Model file does not exist." }
        val engineConfigClass = Class.forName("com.google.ai.edge.litertlm.EngineConfig")
        val backendClass = Class.forName("com.google.ai.edge.litertlm.Backend")
        val cpuBackend = backendClass.declaredClasses.firstOrNull { it.simpleName == "CPU" }
            ?.getDeclaredConstructor()
            ?.newInstance()
            ?: error("LiteRT-LM CPU backend is unavailable.")
        val constructor = engineConfigClass.constructors.firstOrNull { it.parameterTypes.any { type -> type == String::class.java } }
            ?: error("LiteRT-LM EngineConfig API is unavailable.")
        val config = when (constructor.parameterCount) {
            1 -> constructor.newInstance(modelPath)
            else -> constructor.newInstance(modelPath, cpuBackend)
        }
        val engineClass = Class.forName("com.google.ai.edge.litertlm.Engine")
        val engine = engineClass.getConstructor(engineConfigClass).newInstance(config)
        engineClass.methods.firstOrNull { it.name == "initialize" }?.invoke(engine)
        val conversation = engineClass.methods.first { it.name == "createConversation" }.invoke(engine)
        val send = conversation.javaClass.methods.firstOrNull { it.name == "sendMessage" || it.name == "sendMessageSync" }
            ?: error("LiteRT-LM send API is unavailable.")
        return send.invoke(conversation, prompt)?.toString().orEmpty()
    }
}

private fun JSONArray?.toBorromeanCandidates(): List<BorromeanWordCandidate> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                BorromeanWordCandidate(
                    text = item.optString("text"),
                    registerType = item.optString("register_type"),
                    objectPartType = item.optString("object_part_type").takeIf { it.isNotBlank() && it != "null" },
                    emotionalWeight = item.optInt("emotional_weight", 50),
                    importanceWeight = item.optInt("importance_weight", 50),
                    desireWeight = item.optInt("desire_weight", 50),
                    evidence = item.optString("evidence"),
                    confidence = item.optString("confidence", "low"),
                ),
            )
        }
    }
}
