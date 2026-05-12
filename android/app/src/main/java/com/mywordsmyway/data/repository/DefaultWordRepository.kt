package com.mywordsmyway.data.repository

import androidx.room.withTransaction
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.local.NounEntity
import com.mywordsmyway.data.local.NounLinkEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.storage.plainNoteText
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.util.UUID

class DefaultWordRepository(
    private val database: AppDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : WordRepository {
    private val nounDao = database.nounDao()
    private val conversationDao = database.conversationDao()

    override fun observeNouns(): Flow<List<NounWithLinksEntity>> =
        nounDao.observeNounsWithLinks()

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
}
