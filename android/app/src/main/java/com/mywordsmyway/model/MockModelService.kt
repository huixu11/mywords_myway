package com.mywordsmyway.model

import com.mywordsmyway.data.model.NounCandidate
import com.mywordsmyway.data.model.BorromeanExtractionResult
import com.mywordsmyway.data.model.BorromeanWordCandidate
import com.mywordsmyway.data.model.NounExtractionResult
import com.mywordsmyway.data.model.SUPPORT_MESSAGE
import com.mywordsmyway.data.model.SafetyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MockModelService : ModelService {
    private val concreteNouns = listOf(
        "phone", "window", "door", "kitchen", "bed", "school", "dress", "hair",
        "eyes", "smile", "face", "mouth", "hand", "breast", "voice", "room",
        "light", "milk", "table", "photo", "toy", "suitcase", "key", "home",
        "car", "rain", "station", "hospital", "letter", "mirror", "bag", "excrement",
    )

    private val aliases = mapOf(
        "breast" to listOf("chest", "bosom", "boob", "nipple"),
        "excrement" to listOf(
            "feces", "faeces", "poop", "poo", "pooped", "stool",
            "bowel movement", "defecation", "defecated", "shit",
        ),
        "eyes" to listOf("eye", "gaze"),
        "smile" to listOf("grin"),
        "mouth" to listOf("lips", "lip"),
        "hand" to listOf("hands", "palm", "finger"),
        "photo" to listOf("picture", "photograph"),
        "phone" to listOf("telephone"),
        "suitcase" to listOf("luggage"),
        "bag" to listOf("purse"),
        "home" to listOf("house"),
        "car" to listOf("automobile"),
    )

    private val appearanceNouns = setOf("hair", "eyes", "smile", "face", "mouth", "hand", "breast", "voice")
    private val childOfferNouns = setOf("excrement")
    private val currentTriggerTerms = setOf(
        "today", "now", "currently", "this morning", "tonight", "yesterday",
        "work", "office", "meeting", "boss", "friend", "partner", "boyfriend",
        "girlfriend", "husband", "wife", "child", "children", "text", "message",
        "email", "call", "apartment",
    )

    override suspend fun transcribe(audioPath: String): String = withContext(Dispatchers.Default) {
        "Today I kept checking my phone after someone disappeared. " +
            "I do not know why I wait when someone disappears. " +
            "When I was little, I sat near the window waiting for my mom to come home. " +
            "I remember her hair, her eyes, and the way her smile changed when she came in. " +
            "I remember being little and feeling proud when I pooped because my mom smiled. " +
            "The room was quiet. There was light outside. " +
            "I thought if I was quiet and good, she would come back happy."
    }

    override suspend fun safetyCheck(text: String): SafetyResult = withContext(Dispatchers.Default) {
        val lowered = text.lowercase()
        val riskyTerms = listOf(
            "kill myself",
            "suicide",
            "hurt myself",
            "end my life",
            "want to die",
            "self harm",
            "cut myself",
        )
        if (riskyTerms.any { lowered.contains(it) }) {
            SafetyResult(
                riskLevel = "high",
                riskTypes = listOf("self_harm"),
                shouldStopReflectionLoop = false,
                safeUserMessage = SUPPORT_MESSAGE,
            )
        } else {
            SafetyResult(
                riskLevel = "none",
                riskTypes = emptyList(),
                shouldStopReflectionLoop = false,
                safeUserMessage = "",
            )
        }
    }

    override suspend fun extractNouns(
        sourceText: String,
        existingNouns: List<String>,
    ): NounExtractionResult = withContext(Dispatchers.Default) {
        val existing = existingNouns.map { it.uppercase() }.toSet()
        val lowered = sourceText.lowercase()
        val found = concreteNouns.mapNotNull { noun ->
            val match = findMatch(noun, lowered) ?: return@mapNotNull null
            val sceneType = sceneTypeFor(noun, lowered, match.first, match.last)
            val reason = visibleReasonFor(noun, sceneType)
            val start = (match.first - 70).coerceAtLeast(0)
            val end = (match.first + 100).coerceAtMost(sourceText.length)
            NounCandidate(
                noun = noun.uppercase(),
                sceneType = sceneType,
                evidenceSpan = sourceText.substring(start, end).trim(),
                visibleReason = reason,
                confidence = if (noun == "light") "low" else "medium",
                suggestion = if (noun.uppercase() in existing) "update_existing" else "add",
            )
        }.take(10)
        NounExtractionResult(candidateNouns = found)
    }

    override suspend fun extractBorromeanWords(
        sourceText: String,
        existingWords: List<String>,
    ): BorromeanExtractionResult = withContext(Dispatchers.Default) {
        val existing = existingWords.map { it.trim().lowercase() }.toSet()
        val lowered = sourceText.lowercase()
        val objectWords = listOf("eyes", "smile", "hair", "voice", "breast", "excrement")
        val affectWords = listOf("respect", "money", "good person", "beautiful", "warm")
        val desireWords = listOf("solve scientific problems", "earn money", "make my own choice", "study freely")
        val candidates = buildList {
            objectWords.forEach { word ->
                if (lowered.contains(word) && word !in existing) {
                    add(
                        BorromeanWordCandidate(
                            text = word,
                            registerType = "object_a",
                            objectPartType = objectPartTypeFor(word),
                            emotionalWeight = 72,
                            importanceWeight = 70,
                            evidence = evidenceFor(sourceText, word),
                            confidence = "medium",
                        ),
                    )
                }
            }
            affectWords.forEach { word ->
                if (lowered.contains(word) && word !in existing) {
                    add(
                        BorromeanWordCandidate(
                            text = word,
                            registerType = "affect",
                            emotionalWeight = 78,
                            importanceWeight = 78,
                            evidence = evidenceFor(sourceText, word),
                            confidence = "medium",
                        ),
                    )
                }
            }
            desireWords.forEach { word ->
                if (lowered.contains(word) && word !in existing) {
                    add(
                        BorromeanWordCandidate(
                            text = word,
                            registerType = "desire",
                            desireWeight = 82,
                            importanceWeight = 76,
                            evidence = evidenceFor(sourceText, word),
                            confidence = "medium",
                        ),
                    )
                }
            }
        }
        BorromeanExtractionResult(candidateWords = candidates.take(12))
    }

    override suspend fun modelStatus(): String = "Mock Gemma extraction"

    private fun findMatch(noun: String, text: String): IntRange? {
        val terms = (listOf(noun) + aliases.orEmpty(noun)).sortedByDescending { it.length }
        return terms.firstNotNullOfOrNull { term ->
            val suffix = if (term.endsWith("s")) "" else "s?"
            Regex("\\b${Regex.escape(term)}$suffix\\b").find(text)?.range
        }
    }

    private fun objectPartTypeFor(word: String): String? =
        when (word) {
            "eyes", "smile", "hair" -> "gaze"
            "voice" -> "voice"
            "breast" -> "breast"
            "excrement" -> "excrement"
            else -> null
        }

    private fun evidenceFor(sourceText: String, word: String): String {
        val match = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE).find(sourceText) ?: return ""
        val start = (match.range.first - 80).coerceAtLeast(0)
        val end = (match.range.last + 100).coerceAtMost(sourceText.length)
        return sourceText.substring(start, end).trim()
    }

    private fun sceneTypeFor(noun: String, text: String, start: Int, end: Int): String =
        when {
            noun in listOf("window", "door", "room", "light") -> "mom_left"
            noun in appearanceNouns -> "with_mom"
            noun in childOfferNouns && hasChildOfferContext(text, start, end) -> "what_mom_wanted"
            hasCurrentContext(text, start, end) -> "current_trigger"
            else -> "unclear"
        }

    private fun visibleReasonFor(noun: String, sceneType: String): String =
        when {
            noun in appearanceNouns ->
                "This noun appeared near a remembered appearance detail in a childhood/mother-related part of your note."
            noun in childOfferNouns ->
                "This noun appeared near an early childhood bodily-control scene connected to mother, play, offering, or making mother happy."
            sceneType == "current_trigger" ->
                "This noun appeared in a current scene that you associated with earlier memories."
            else ->
                "This noun appeared in a current scene or childhood/mother-related part of your note."
        }

    private fun hasChildOfferContext(text: String, start: Int, end: Int): Boolean {
        val context = text.substring((start - 90).coerceAtLeast(0), (end + 130).coerceAtMost(text.length))
        val motherTerms = listOf("mom", "mother", "mama", "mommy")
        val offerTerms = listOf("happy", "smile", "smiled", "gift", "present", "proud", "pleased", "control", "play", "playful")
        return motherTerms.any { context.contains(it) } && offerTerms.any { context.contains(it) }
    }

    private fun hasCurrentContext(text: String, start: Int, end: Int): Boolean {
        val context = text.substring((start - 90).coerceAtLeast(0), (end + 130).coerceAtMost(text.length))
        return currentTriggerTerms.any { term -> Regex("\\b${Regex.escape(term)}\\b").containsMatchIn(context) }
    }
}

private fun <K, V> Map<K, List<V>>.orEmpty(key: K): List<V> = this[key] ?: emptyList()
