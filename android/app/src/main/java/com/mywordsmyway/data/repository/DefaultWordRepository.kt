package com.mywordsmyway.data.repository

import androidx.room.withTransaction
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.local.BorromeanKnotEntity
import com.mywordsmyway.data.local.BorromeanKnotWithWords
import com.mywordsmyway.data.local.BorromeanWordEntity
import com.mywordsmyway.data.local.NounEntity
import com.mywordsmyway.data.local.NounLinkEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.model.BorromeanWordCandidate
import com.mywordsmyway.storage.plainNoteText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import java.time.Clock
import java.util.UUID

class DefaultWordRepository(
    private val database: AppDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : WordRepository {
    private val nounDao = database.nounDao()
    private val borromeanDao = database.borromeanDao()
    private val conversationDao = database.conversationDao()

    override fun observeNouns(): Flow<List<NounWithLinksEntity>> =
        nounDao.observeNounsWithLinks()

    override fun observeBorromeanKnots(): Flow<List<BorromeanKnotWithWords>> =
        borromeanDao.observeKnotsWithWords().onStart {
            normalizeLegacyBorromeanWords()
        }

    override fun observeGlobalBorromeanWords(): Flow<List<BorromeanWordEntity>> =
        borromeanDao.observeGlobalWords().onStart {
            normalizeLegacyBorromeanWords()
        }

    override fun observeSuggestions(conversationId: String): Flow<List<NounSuggestionEntity>> =
        nounDao.observeSuggestionsForConversation(conversationId)

    override fun observePendingSuggestions(): Flow<List<NounSuggestionEntity>> =
        nounDao.observePendingSuggestions()

    override suspend fun keepSuggestion(suggestionId: String) {
        database.withTransaction {
            val suggestion = nounDao.getSuggestion(suggestionId) ?: return@withTransaction
            if (suggestion.status != "pending") return@withTransaction
            val now = clock.instant()
            val normalized = suggestion.suggestedNoun.trim().uppercase()
            if (normalized.isEmpty()) return@withTransaction
            val noun = nounDao.findConfirmedByName(normalized) ?: NounEntity(
                id = UUID.randomUUID().toString(),
                noun = normalized,
                status = "confirmed",
                createdAt = now,
                updatedAt = now,
            ).also { nounDao.insertNoun(it) }
            val note = plainNoteText(conversationDao.getConversation(suggestion.conversationId)?.finalNote.orEmpty())
            nounDao.insertLink(
                NounLinkEntity(
                    id = UUID.randomUUID().toString(),
                    nounId = noun.id,
                    conversationId = suggestion.conversationId,
                    memoId = null,
                    transcriptSpan = suggestion.evidenceSpan,
                    visibleNoteExcerpt = note,
                    sceneType = suggestion.sceneType,
                    createdAt = now,
                ),
            )
            nounDao.updateSuggestionStatus(suggestionId, "accepted")
        }
    }

    override suspend fun renameSuggestion(suggestionId: String, newNoun: String) {
        val normalized = newNoun.trim().uppercase()
        if (normalized.isNotEmpty()) {
            nounDao.renameSuggestion(suggestionId, normalized)
        }
    }

    override suspend fun rejectSuggestion(suggestionId: String) {
        nounDao.updateSuggestionStatus(suggestionId, "rejected")
    }

    override suspend fun renameNoun(nounId: String, newNoun: String) {
        val normalized = newNoun.trim().uppercase()
        if (normalized.isEmpty()) return
        database.withTransaction {
            val existing = nounDao.findConfirmedByName(normalized)
            if (existing != null && existing.id != nounId) {
                nounDao.moveLinks(sourceNounId = nounId, targetNounId = existing.id)
                nounDao.deleteNoun(nounId)
            } else {
                nounDao.renameNoun(nounId, normalized, clock.instant())
            }
        }
    }

    override suspend fun mergeNoun(sourceNounId: String, targetNounId: String) {
        if (sourceNounId == targetNounId) return
        database.withTransaction {
            nounDao.moveLinks(sourceNounId = sourceNounId, targetNounId = targetNounId)
            nounDao.deleteNoun(sourceNounId)
        }
    }

    override suspend fun deleteNoun(nounId: String) {
        nounDao.deleteNoun(nounId)
    }

    override suspend fun deleteAllNouns() {
        nounDao.deleteAllNouns()
    }

    override suspend fun deletePendingSuggestions() {
        nounDao.deletePendingSuggestions()
    }

    override suspend fun createBorromeanKnot(title: String, description: String, personName: String?, theoryNote: String): String {
        val cleanTitle = title.trim().ifBlank { "Untitled knot" }
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        borromeanDao.insertKnot(
            BorromeanKnotEntity(
                id = id,
                title = cleanTitle,
                description = description.trim(),
                personName = personName?.trim()?.takeIf { it.isNotEmpty() },
                theoryNote = theoryNote.trim(),
                createdAt = now,
                updatedAt = now,
                sortOrder = borromeanDao.countKnots(),
                isArchived = false,
            ),
        )
        return id
    }

    override suspend fun renameBorromeanKnot(knotId: String, title: String, description: String, personName: String?, theoryNote: String) {
        borromeanDao.updateKnot(
            knotId = knotId,
            title = title.trim().ifBlank { "Untitled knot" },
            description = description.trim(),
            personName = personName?.trim()?.takeIf { it.isNotEmpty() },
            theoryNote = theoryNote.trim(),
            updatedAt = clock.instant(),
        )
    }

    override suspend fun archiveBorromeanKnot(knotId: String) {
        borromeanDao.archiveKnot(knotId, clock.instant())
    }

    override suspend fun addBorromeanWord(
        knotId: String?,
        text: String,
        registerType: String,
        objectPartType: String?,
        emotionalWeight: Int,
        importanceWeight: Int,
        desireWeight: Int,
    ): String {
        val cleanText = text.trim()
        require(cleanText.isNotEmpty()) { "Word is required." }
        val cleanRegister = requireValidBorromeanRegister(registerType)
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        borromeanDao.insertWord(
            BorromeanWordEntity(
                id = id,
                knotId = knotId,
                text = cleanText,
                registerType = cleanRegister,
                objectPartType = objectPartType?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
                emotionalWeight = emotionalWeight.coerceIn(0, 100),
                importanceWeight = importanceWeight.coerceIn(0, 100),
                desireWeight = desireWeight.coerceIn(0, 100),
                conversationId = null,
                createdAt = now,
                updatedAt = now,
                sortOrder = if (knotId == null) {
                    borromeanDao.countGlobalWords(cleanRegister)
                } else {
                    borromeanDao.countWords(knotId, cleanRegister)
                },
                source = "manual",
                sourceConversationId = null,
                sourceMemoId = null,
                extractionEvidence = null,
                extractedAt = null,
            ),
        )
        return id
    }

    override suspend fun renameBorromeanWord(wordId: String, text: String) {
        val cleanText = text.trim()
        require(cleanText.isNotEmpty()) { "Word is required." }
        borromeanDao.updateWord(wordId, cleanText, clock.instant())
    }

    override suspend fun updateBorromeanWordDetails(
        wordId: String,
        text: String,
        objectPartType: String?,
        emotionalWeight: Int,
        importanceWeight: Int,
        desireWeight: Int,
    ) {
        val cleanText = text.trim()
        require(cleanText.isNotEmpty()) { "Word is required." }
        borromeanDao.updateWordDetails(
            wordId = wordId,
            text = cleanText,
            objectPartType = objectPartType?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
            emotionalWeight = emotionalWeight.coerceIn(0, 100),
            importanceWeight = importanceWeight.coerceIn(0, 100),
            desireWeight = desireWeight.coerceIn(0, 100),
            updatedAt = clock.instant(),
        )
    }

    override suspend fun updateBorromeanWordWeights(wordId: String, emotionalWeight: Int, importanceWeight: Int, desireWeight: Int) {
        borromeanDao.updateWordWeights(
            wordId = wordId,
            emotionalWeight = emotionalWeight.coerceIn(0, 100),
            importanceWeight = importanceWeight.coerceIn(0, 100),
            desireWeight = desireWeight.coerceIn(0, 100),
            updatedAt = clock.instant(),
        )
    }

    override suspend fun deleteBorromeanWord(wordId: String) {
        borromeanDao.deleteWord(wordId)
    }

    override suspend fun linkBorromeanWordToConversation(wordId: String, conversationId: String) {
        borromeanDao.linkWordToConversation(wordId, conversationId, clock.instant())
    }

    override suspend fun saveExtractedBorromeanWords(
        conversationId: String,
        objectKnotId: String?,
        candidates: List<BorromeanWordCandidate>,
    ): Int {
        val now = clock.instant()
        var inserted = 0
        database.withTransaction {
            var resolvedObjectKnotId = objectKnotId
            if (resolvedObjectKnotId == null && candidates.any { it.registerType.trim().lowercase() == "object_a" }) {
                val note = conversationDao.getConversation(conversationId)
                val knotId = UUID.randomUUID().toString()
                borromeanDao.insertKnot(
                    BorromeanKnotEntity(
                        id = knotId,
                        title = note?.title
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() && it != "Untitled reflection" && it != "Untitled note" }
                            ?.let { "Gemma: $it" }
                            ?: "Gemma extracted Borromean knot",
                        description = "Object a words extracted from a saved note and its voice memos.",
                        personName = null,
                        theoryNote = "The knot is generated from Gemma-extracted object a words in the user's own notes.",
                        createdAt = now,
                        updatedAt = now,
                        sortOrder = borromeanDao.countKnots(),
                        isArchived = false,
                    ),
                )
                resolvedObjectKnotId = knotId
            }
            candidates.forEach { candidate ->
                val register = runCatching { requireValidBorromeanRegister(candidate.registerType) }.getOrNull() ?: return@forEach
                val text = candidate.text.trim()
                if (text.isBlank()) return@forEach
                val knotId = if (register == "object_a") resolvedObjectKnotId else null
                if (register == "object_a" && knotId == null) return@forEach
                val existing = borromeanDao.findWordByTextAndRegister(text, register, knotId)
                if (existing != null) return@forEach
                borromeanDao.insertWord(
                    BorromeanWordEntity(
                        id = UUID.randomUUID().toString(),
                        knotId = knotId,
                        text = text,
                        registerType = register,
                        objectPartType = candidate.objectPartType?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
                        emotionalWeight = candidate.emotionalWeight.coerceIn(0, 100),
                        importanceWeight = candidate.importanceWeight.coerceIn(0, 100),
                        desireWeight = candidate.desireWeight.coerceIn(0, 100),
                        conversationId = conversationId,
                        createdAt = now,
                        updatedAt = now,
                        sortOrder = if (knotId == null) {
                            borromeanDao.countGlobalWords(register)
                        } else {
                            borromeanDao.countWords(knotId, register)
                        },
                        source = "gemma",
                        sourceConversationId = conversationId,
                        sourceMemoId = candidate.sourceMemoId,
                        extractionEvidence = candidate.evidence.takeIf { it.isNotBlank() },
                        extractedAt = now,
                    ),
                )
                inserted += 1
            }
        }
        return inserted
    }

    private suspend fun normalizeLegacyBorromeanWords() {
        borromeanDao.replaceWordText(
            oldText = "粪便",
            newText = "excrement",
            updatedAt = clock.instant(),
        )
    }

    private fun requireValidBorromeanRegister(registerType: String): String {
        val clean = registerType.trim().lowercase()
        require(clean in validBorromeanRegisters) { "Unknown register: $registerType" }
        return clean
    }
}

private val validBorromeanRegisters = setOf("object_a", "real", "symbolic", "imaginary", "affect", "desire")
