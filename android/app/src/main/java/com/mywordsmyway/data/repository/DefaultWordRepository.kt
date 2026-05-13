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
        borromeanDao.observeKnotsWithWords().onStart {
            normalizeLegacyBorromeanWords()
            ensureStarterBorromeanKnots()
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
        knotId: String,
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
                        personName = starter.personName,
                        theoryNote = starter.theoryNote,
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
                            objectPartType = word.objectPartType,
                            emotionalWeight = word.emotionalWeight,
                            importanceWeight = word.importanceWeight,
                            desireWeight = word.desireWeight,
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

private data class StarterBorromeanKnot(
    val title: String,
    val description: String,
    val personName: String?,
    val theoryNote: String,
    val words: List<StarterBorromeanWord>,
)

private data class StarterBorromeanWord(
    val text: String,
    val registerType: String,
    val objectPartType: String? = null,
    val emotionalWeight: Int = 50,
    val importanceWeight: Int = 50,
    val desireWeight: Int = 50,
)

private val validBorromeanRegisters = setOf("object_a", "real", "symbolic", "imaginary", "affect", "desire")

private val starterBorromeanKnots = listOf(
    StarterBorromeanKnot(
        title = "Mother / first object a",
        description = "S1 calls a field of memories; the leftover pull appears around the gaze, voice, breast, and excrement.",
        personName = "Mother / first object a",
        theoryNote = "The knot can stretch or rotate, but the topology remains. A new knot forms when desire reorganizes around another object/cause.",
        words = listOf(
            StarterBorromeanWord("gaze / eyes", "object_a", objectPartType = "gaze"),
            StarterBorromeanWord("voice", "object_a", objectPartType = "voice"),
            StarterBorromeanWord("breast", "object_a", objectPartType = "breast"),
            StarterBorromeanWord("excrement", "object_a", objectPartType = "excrement"),
            StarterBorromeanWord("respect", "affect", emotionalWeight = 86, importanceWeight = 78),
            StarterBorromeanWord("money", "affect", emotionalWeight = 74, importanceWeight = 82),
            StarterBorromeanWord("good person", "affect", emotionalWeight = 70, importanceWeight = 76),
            StarterBorromeanWord("solve scientific problems", "desire", desireWeight = 88, importanceWeight = 84),
            StarterBorromeanWord("earn money", "desire", desireWeight = 68, importanceWeight = 80),
            StarterBorromeanWord("make my own choice", "desire", desireWeight = 92, importanceWeight = 88),
        ),
    ),
    StarterBorromeanKnot(
        title = "Rain At The Window",
        description = "Another possible knot from older notes.",
        personName = "Rain At The Window",
        theoryNote = "The knot can stretch or rotate, but the topology remains.",
        words = listOf(
            StarterBorromeanWord("wet pavement", "object_a", objectPartType = "gaze"),
            StarterBorromeanWord("waiting", "affect", emotionalWeight = 66, importanceWeight = 52),
            StarterBorromeanWord("leave first", "desire", desireWeight = 61),
        ),
    ),
    StarterBorromeanKnot(
        title = "Library Afternoon",
        description = "A quieter constellation of object a words.",
        personName = "Library Afternoon",
        theoryNote = "The knot can stretch or rotate, but the topology remains.",
        words = listOf(
            StarterBorromeanWord("pencil dust", "object_a", objectPartType = "excrement"),
            StarterBorromeanWord("quiet", "affect", emotionalWeight = 48, importanceWeight = 65),
            StarterBorromeanWord("study freely", "desire", desireWeight = 74),
        ),
    ),
)
