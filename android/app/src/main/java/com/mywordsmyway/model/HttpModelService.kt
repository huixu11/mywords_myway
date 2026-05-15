package com.mywordsmyway.model

import com.mywordsmyway.data.model.BorromeanExtractionResult
import com.mywordsmyway.data.model.BorromeanWordCandidate
import com.mywordsmyway.data.model.NounCandidate
import com.mywordsmyway.data.model.NounExtractionResult
import com.mywordsmyway.data.model.SafetyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpModelService(
    private val baseUrl: String,
) : ModelService {
    override suspend fun transcribe(audioPath: String): String = withContext(Dispatchers.IO) {
        postJson(
            path = "/transcribe",
            body = JSONObject().put("audio_path", audioPath),
        ).optString("transcript")
    }

    override suspend fun safetyCheck(text: String): SafetyResult = withContext(Dispatchers.IO) {
        val json = postJson(
            path = "/safety",
            body = JSONObject().put("text", text),
        )
        SafetyResult(
            riskLevel = json.optString("risk_level", "none"),
            riskTypes = json.optJSONArray("risk_types").toStringList(),
            shouldStopReflectionLoop = json.optBoolean("should_stop_reflection_loop", false),
            safeUserMessage = json.optString("safe_user_message", ""),
        )
    }

    override suspend fun extractNouns(
        sourceText: String,
        existingNouns: List<String>,
    ): NounExtractionResult = withContext(Dispatchers.IO) {
        val existing = JSONArray().also { array -> existingNouns.forEach { array.put(it) } }
        val json = postJson(
            path = "/extract-nouns",
            body = JSONObject()
                .put("source_text", sourceText)
                .put("existing_nouns", existing),
        )
        val candidates = json.optJSONArray("candidate_nouns").toCandidates()
        NounExtractionResult(candidateNouns = candidates)
    }

    override suspend fun extractBorromeanWords(
        sourceText: String,
        existingWords: List<String>,
    ): BorromeanExtractionResult = withContext(Dispatchers.IO) {
        val existing = JSONArray().also { array -> existingWords.forEach { array.put(it) } }
        val json = postJson(
            path = "/extract-borromean-words",
            body = JSONObject()
                .put("source_text", sourceText)
                .put("existing_words", existing),
        )
        BorromeanExtractionResult(candidateWords = json.optJSONArray("candidate_words").toBorromeanCandidates())
    }

    private fun postJson(path: String, body: JSONObject): JSONObject {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
        }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            error("Model service returned HTTP ${connection.responseCode}: $text")
        }
        return JSONObject(text)
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) add(optString(index))
    }
}

private fun JSONArray?.toCandidates(): List<NounCandidate> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                NounCandidate(
                    noun = item.optString("noun").uppercase(),
                    sceneType = item.optString("scene_type", "unclear"),
                    evidenceSpan = item.optString("evidence_span", ""),
                    visibleReason = item.optString("visible_reason", "This noun appeared in your note."),
                    confidence = item.optString("confidence", "low"),
                    suggestion = item.optString("suggestion", "add"),
                ),
            )
        }
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
                    sourceMemoId = item.optString("source_memo_id").takeIf { it.isNotBlank() && it != "null" },
                ),
            )
        }
    }
}
