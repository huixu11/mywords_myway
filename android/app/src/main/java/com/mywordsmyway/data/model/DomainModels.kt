package com.mywordsmyway.data.model

import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.local.VoiceMemoEntity

const val LISTENING_QUESTION = "What does this make you think about?"
const val SUPPORT_MESSAGE =
    "I'm listening. If you might hurt yourself, call or text 988 to reach the 988 Suicide & Crisis Lifeline. If there is immediate danger, call 911."

data class WeeklyAccess(
    val freeConversationAvailable: Boolean,
)

data class StartConversationResult(
    val conversation: ConversationEntity,
    val question: String = LISTENING_QUESTION,
)

data class AddMemoResult(
    val memo: VoiceMemoEntity,
    val memoCount: Int,
    val safety: SafetyResult,
    val nextQuestion: String = LISTENING_QUESTION,
)

data class SafetyResult(
    val riskLevel: String,
    val riskTypes: List<String>,
    val shouldStopReflectionLoop: Boolean,
    val safeUserMessage: String,
)

data class NounCandidate(
    val noun: String,
    val sceneType: String,
    val evidenceSpan: String,
    val visibleReason: String,
    val confidence: String,
    val suggestion: String,
)

data class NounExtractionResult(
    val candidateNouns: List<NounCandidate>,
)

data class BorromeanWordCandidate(
    val text: String,
    val registerType: String,
    val objectPartType: String? = null,
    val emotionalWeight: Int = 50,
    val importanceWeight: Int = 50,
    val desireWeight: Int = 50,
    val evidence: String = "",
    val confidence: String = "low",
    val sourceMemoId: String? = null,
)

data class BorromeanExtractionResult(
    val candidateWords: List<BorromeanWordCandidate>,
)

data class StorageUsage(
    val usedBytes: Long,
    val limitBytes: Long,
)

data class NotesOnlyExport(
    val fileName: String,
    val content: String,
)

data class NoteFileAttachment(
    val displayName: String,
    val filePath: String,
    val mimeType: String,
)

typealias Conversation = ConversationEntity
typealias NounSuggestion = NounSuggestionEntity
typealias NounWithLinks = NounWithLinksEntity
