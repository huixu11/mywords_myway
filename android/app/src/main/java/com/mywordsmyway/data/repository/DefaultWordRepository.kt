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
        borromeanDao.observeKnotsWithWords().onStart { ensureStarterBorromeanKnots() }

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

    override suspend fun createBorromeanKnot(title: String, description: String): String {
        val cleanTitle = title.trim().ifBlank { "Untitled knot" }
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        borromeanDao.insertKnot(
            BorromeanKnotEntity(
                id = id,
                title = cleanTitle,
                description = description.trim(),
                createdAt = now,
                updatedAt = now,
                sortOrder = borromeanDao.countKnots(),
                isArchived = false,
            ),
        )
        return id
    }

    override suspend fun renameBorromeanKnot(knotId: String, title: String, description: String) {
        borromeanDao.updateKnot(
            knotId = knotId,
            title = title.trim().ifBlank { "Untitled knot" },
            description = description.trim(),
            updatedAt = clock.instant(),
        )
    }

    override suspend fun archiveBorromeanKnot(knotId: String) {
        borromeanDao.archiveKnot(knotId, clock.instant())
    }

    override suspend fun addBorromeanWord(knotId: String, text: String, registerType: String): String {
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
                conversationId = null,
                createdAt = now,
                updatedAt = now,
                sortOrder = borromeanDao.countWords(knotId, cleanRegister),
            ),
        )
        return id
    }

    override suspend fun renameBorromeanWord(wordId: String, text: String) {
        val cleanText = text.trim()
        require(cleanText.isNotEmpty()) { "Word is required." }
        borromeanDao.updateWord(wordId, cleanText, clock.instant())
    }

    override suspend fun deleteBorromeanWord(wordId: String) {
        borromeanDao.deleteWord(wordId)
    }

    override suspend fun linkBorromeanWordToConversation(wordId: String, conversationId: String) {
        borromeanDao.linkWordToConversation(wordId, conversationId, clock.instant())
    }

    private suspend fun ensureStarterBorromeanKnots() {
        if (borromeanDao.countKnots() > 0) return
        database.withTransaction {
            if (borromeanDao.countKnots() > 0) return@withTransaction
            starterBorromeanKnots.forEachIndexed { knotIndex, starter ->
                val now = clock.instant()
                val knotId = UUID.randomUUID().toString()
                borromeanDao.insertKnot(
                    BorromeanKnotEntity(
                        id = knotId,
                        title = starter.title,
                        description = starter.description,
                        createdAt = now,
                        updatedAt = now,
                        sortOrder = knotIndex,
                        isArchived = false,
                    ),
                )
                starter.words.forEachIndexed { wordIndex, word ->
                    borromeanDao.insertWord(
                        BorromeanWordEntity(
                            id = UUID.randomUUID().toString(),
                            knotId = knotId,
                            text = word.text,
                            registerType = word.registerType,
                            conversationId = null,
                            createdAt = now,
                            updatedAt = now,
                            sortOrder = wordIndex,
                        ),
                    )
                }
            }
        }
    }

    private fun requireValidBorromeanRegister(registerType: String): String {
        val clean = registerType.trim().lowercase()
        require(clean in validBorromeanRegisters) { "Unknown register: $registerType" }
        return clean
    }
}

private data class StarterBorromeanKnot(
    val title: String,
    val description: String,
    val words: List<StarterBorromeanWord>,
)

private data class StarterBorromeanWord(
    val text: String,
    val registerType: String,
)

private val validBorromeanRegisters = setOf("object_a", "real", "symbolic", "imaginary")

private val starterBorromeanKnots = listOf(
    StarterBorromeanKnot(
        title = "A person who carries object a",
        description = "The words gather around one person and the missing center they make visible.",
        words = listOf(
            StarterBorromeanWord("chilled mung bean soup", "object_a"),
            StarterBorromeanWord("eyes when she smiles", "object_a"),
            StarterBorromeanWord("her hair", "object_a"),
            StarterBorromeanWord("my city", "real"),
            StarterBorromeanWord("dinner", "real"),
            StarterBorromeanWord("reluctant", "real"),
            StarterBorromeanWord("respect", "real"),
            StarterBorromeanWord("beautiful", "real"),
            StarterBorromeanWord("appearance like my mom", "real"),
            StarterBorromeanWord("money", "symbolic"),
            StarterBorromeanWord("good person", "symbolic"),
            StarterBorromeanWord("respect", "symbolic"),
            StarterBorromeanWord("awkward", "imaginary"),
            StarterBorromeanWord("warm", "imaginary"),
            StarterBorromeanWord("cares about me", "imaginary"),
            StarterBorromeanWord("breast", "imaginary"),
            StarterBorromeanWord("kiss", "imaginary"),
        ),
    ),
    StarterBorromeanKnot(
        title = "Rain At The Window",
        description = "Another possible knot from older notes.",
        words = listOf(
            StarterBorromeanWord("wet pavement", "object_a"),
            StarterBorromeanWord("blue umbrella", "real"),
            StarterBorromeanWord("the pause before yes", "symbolic"),
            StarterBorromeanWord("coffee steam", "imaginary"),
        ),
    ),
    StarterBorromeanKnot(
        title = "Library Afternoon",
        description = "A quieter constellation of object a words.",
        words = listOf(
            StarterBorromeanWord("pencil dust", "object_a"),
            StarterBorromeanWord("green desk lamp", "real"),
            StarterBorromeanWord("wrist near the margin", "imaginary"),
        ),
    ),
)
