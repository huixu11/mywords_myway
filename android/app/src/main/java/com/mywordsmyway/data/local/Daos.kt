package com.mywordsmyway.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observeConversation(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM note_folders WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultNoteFolder(): NoteFolderEntity?

    @Query("SELECT COUNT(*) FROM note_folders")
    suspend fun countNoteFolders(): Int

    @Query(
        """
        SELECT note_folders.*, COUNT(conversations.id) AS noteCount
        FROM note_folders
        LEFT JOIN conversations ON conversations.folderId = note_folders.id
            AND TRIM(conversations.finalNote) != ''
        GROUP BY note_folders.id
        ORDER BY note_folders.sortOrder ASC, note_folders.createdAt ASC
        """,
    )
    fun observeNoteFolders(): Flow<List<NoteFolderWithCount>>

    @Query("SELECT * FROM conversations WHERE finalNote = '' ORDER BY createdAt DESC LIMIT 1")
    fun observeCurrentConversation(): Flow<ConversationEntity?>

    @Query(
        """
        SELECT conversations.*, COUNT(voice_memos.id) AS memoCount
        FROM conversations
        LEFT JOIN voice_memos ON voice_memos.conversationId = conversations.id
        GROUP BY conversations.id
        HAVING memoCount > 0 OR TRIM(finalNote) != ''
        ORDER BY conversations.createdAt DESC
        """,
    )
    fun observeConversationHistory(): Flow<List<ConversationSummaryEntity>>

    @Query("SELECT COUNT(*) FROM conversations WHERE isFreeWeekly = 1 AND createdAt >= :weekStart")
    suspend fun countFreeConversationsSince(weekStart: Instant): Int

    @Query("SELECT COUNT(*) FROM conversations WHERE isFreeWeekly = 1 AND createdAt >= :weekStart")
    fun observeFreeConversationsSince(weekStart: Instant): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNoteFolder(folder: NoteFolderEntity)

    @Query("UPDATE note_folders SET name = :name WHERE id = :folderId AND isDefault = 0")
    suspend fun renameNoteFolder(folderId: String, name: String)

    @Query("DELETE FROM note_folders WHERE id = :folderId AND isDefault = 0")
    suspend fun deleteNoteFolder(folderId: String)

    @Query("UPDATE conversations SET finalNote = :finalNote, title = :title WHERE id = :conversationId")
    suspend fun updateFinalNote(conversationId: String, finalNote: String, title: String)

    @Query("UPDATE conversations SET folderId = :folderId WHERE id = :conversationId")
    suspend fun updateConversationFolder(conversationId: String, folderId: String?)

    @Query("UPDATE conversations SET folderId = :targetFolderId WHERE folderId = :sourceFolderId")
    suspend fun moveConversationsToFolder(sourceFolderId: String, targetFolderId: String?)

    @Query("UPDATE conversations SET safetyStatus = :safetyStatus WHERE id = :conversationId")
    suspend fun updateSafetyStatus(conversationId: String, safetyStatus: String)

    @Query("UPDATE conversations SET isLocked = 1, passwordSalt = :salt, passwordHash = :hash WHERE id = :conversationId")
    suspend fun lockConversation(conversationId: String, salt: String, hash: String)

    @Query("UPDATE conversations SET isLocked = 1, passwordSalt = NULL, passwordHash = NULL WHERE id = :conversationId")
    suspend fun lockConversationWithDeviceAuth(conversationId: String)

    @Query("UPDATE conversations SET isLocked = 0, passwordSalt = NULL, passwordHash = NULL WHERE id = :conversationId")
    suspend fun unlockConversation(conversationId: String)

    @Query(
        """
        SELECT conversations.*
        FROM conversations
        LEFT JOIN voice_memos ON voice_memos.conversationId = conversations.id
        LEFT JOIN note_images ON note_images.conversationId = conversations.id
        WHERE (:folderId IS NULL OR conversations.folderId = :folderId)
          AND (:query = '' OR finalNote LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%')
          AND (:start IS NULL OR conversations.createdAt >= :start)
          AND (:end IS NULL OR conversations.createdAt < :end)
        GROUP BY conversations.id
        HAVING TRIM(finalNote) != ''
          OR COUNT(DISTINCT voice_memos.id) > 0
          OR COUNT(DISTINCT note_images.id) > 0
        ORDER BY conversations.createdAt DESC
        """,
    )
    fun observeNotes(
        folderId: String?,
        query: String,
        start: Instant?,
        end: Instant?,
    ): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT conversations.*
        FROM conversations
        LEFT JOIN voice_memos ON voice_memos.conversationId = conversations.id
        LEFT JOIN note_images ON note_images.conversationId = conversations.id
        WHERE (:folderId IS NULL OR conversations.folderId = :folderId)
          AND (:start IS NULL OR conversations.createdAt >= :start)
          AND (:end IS NULL OR conversations.createdAt < :end)
        GROUP BY conversations.id
        HAVING TRIM(finalNote) != ''
          OR COUNT(DISTINCT voice_memos.id) > 0
          OR COUNT(DISTINCT note_images.id) > 0
        ORDER BY conversations.createdAt DESC
        """,
    )
    fun observeNotesInFolder(
        folderId: String?,
        start: Instant?,
        end: Instant?,
    ): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE TRIM(finalNote) != '' ORDER BY createdAt DESC")
    suspend fun getAllNotes(): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface VoiceMemoDao {
    @Query("SELECT * FROM voice_memos WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeMemosForConversation(conversationId: String): Flow<List<VoiceMemoEntity>>

    @Query("SELECT COUNT(*) FROM voice_memos WHERE conversationId = :conversationId")
    suspend fun countForConversation(conversationId: String): Int

    @Query("SELECT * FROM voice_memos WHERE id = :memoId")
    suspend fun getMemo(memoId: String): VoiceMemoEntity?

    @Query("SELECT transcript FROM voice_memos WHERE conversationId = :conversationId AND transcript IS NOT NULL AND TRIM(transcript) != '' ORDER BY createdAt ASC")
    suspend fun getTranscriptsForConversation(conversationId: String): List<String>

    @Query("SELECT * FROM voice_memos WHERE audioPath IS NOT NULL OR transcript IS NOT NULL ORDER BY createdAt ASC")
    suspend fun getCleanupCandidates(): List<VoiceMemoEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMemo(memo: VoiceMemoEntity)

    @Query("UPDATE voice_memos SET audioPath = NULL, audioDeletedAt = :deletedAt WHERE id = :memoId")
    suspend fun markAudioDeleted(memoId: String, deletedAt: Instant)

    @Query("UPDATE voice_memos SET transcript = NULL, transcriptDeletedAt = :deletedAt WHERE id = :memoId")
    suspend fun markTranscriptDeleted(memoId: String, deletedAt: Instant)

    @Query("UPDATE voice_memos SET transcript = NULL, transcriptDeletedAt = :deletedAt WHERE transcript IS NOT NULL AND TRIM(transcript) != ''")
    suspend fun deleteAllHiddenTranscripts(deletedAt: Instant)

    @Query("UPDATE voice_memos SET audioPath = NULL, audioDeletedAt = :deletedAt WHERE audioPath IS NOT NULL")
    suspend fun markAllAudioDeleted(deletedAt: Instant)
}

@Dao
interface NoteImageDao {
    @Query("SELECT * FROM note_images WHERE conversationId = :conversationId ORDER BY sortOrder ASC, createdAt ASC")
    fun observeImagesForConversation(conversationId: String): Flow<List<NoteImageEntity>>

    @Query("SELECT * FROM note_images WHERE conversationId = :conversationId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getImagesForConversation(conversationId: String): List<NoteImageEntity>

    @Query("SELECT * FROM note_images ORDER BY createdAt ASC")
    suspend fun getAllImages(): List<NoteImageEntity>

    @Query("SELECT COUNT(*) FROM note_images WHERE conversationId = :conversationId")
    suspend fun countForConversation(conversationId: String): Int

    @Query("SELECT * FROM note_images WHERE id = :imageId")
    suspend fun getImage(imageId: String): NoteImageEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertImage(image: NoteImageEntity)

    @Query("DELETE FROM note_images WHERE id = :imageId")
    suspend fun deleteImage(imageId: String)
}

@Dao
interface NounDao {
    @Transaction
    @Query("SELECT * FROM nouns WHERE status = 'confirmed' ORDER BY updatedAt DESC")
    fun observeNounsWithLinks(): Flow<List<NounWithLinksEntity>>

    @Query("SELECT * FROM nouns WHERE id = :nounId")
    suspend fun getNoun(nounId: String): NounEntity?

    @Query("SELECT * FROM nouns WHERE UPPER(noun) = UPPER(:noun) AND status = 'confirmed' LIMIT 1")
    suspend fun findConfirmedByName(noun: String): NounEntity?

    @Query("SELECT noun FROM nouns WHERE status = 'confirmed' ORDER BY noun ASC")
    suspend fun getConfirmedNounNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNoun(noun: NounEntity)

    @Query("UPDATE nouns SET noun = :newNoun, updatedAt = :updatedAt WHERE id = :nounId")
    suspend fun renameNoun(nounId: String, newNoun: String, updatedAt: Instant)

    @Query("DELETE FROM nouns WHERE id = :nounId")
    suspend fun deleteNoun(nounId: String)

    @Query("DELETE FROM nouns")
    suspend fun deleteAllNouns()

    @Query("UPDATE noun_links SET nounId = :targetNounId WHERE nounId = :sourceNounId")
    suspend fun moveLinks(sourceNounId: String, targetNounId: String)

    @Query("UPDATE noun_links SET visibleNoteExcerpt = :visibleNoteExcerpt WHERE conversationId = :conversationId")
    suspend fun updateVisibleNoteExcerpt(conversationId: String, visibleNoteExcerpt: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLink(link: NounLinkEntity)

    @Query("SELECT n.noun AS noun, nl.conversationId AS conversationId FROM nouns n INNER JOIN noun_links nl ON nl.nounId = n.id WHERE n.status = 'confirmed' ORDER BY n.noun ASC")
    suspend fun getExportLinks(): List<NounLinkExportRow>

    @Query("SELECT * FROM noun_suggestions WHERE id = :suggestionId")
    suspend fun getSuggestion(suggestionId: String): NounSuggestionEntity?

    @Query("SELECT * FROM noun_suggestions WHERE conversationId = :conversationId AND status = :status ORDER BY createdAt ASC")
    fun observeSuggestionsForConversation(conversationId: String, status: String = "pending"): Flow<List<NounSuggestionEntity>>

    @Query("SELECT * FROM noun_suggestions WHERE status = 'pending' ORDER BY createdAt DESC")
    fun observePendingSuggestions(): Flow<List<NounSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSuggestion(suggestion: NounSuggestionEntity)

    @Query("UPDATE noun_suggestions SET suggestedNoun = :newNoun WHERE id = :suggestionId")
    suspend fun renameSuggestion(suggestionId: String, newNoun: String)

    @Query("UPDATE noun_suggestions SET status = :status WHERE id = :suggestionId")
    suspend fun updateSuggestionStatus(suggestionId: String, status: String)

    @Query("DELETE FROM noun_suggestions WHERE status = 'pending'")
    suspend fun deletePendingSuggestions()
}

@Dao
interface BorromeanDao {
    @Transaction
    @Query("SELECT * FROM borromean_knots WHERE isArchived = 0 ORDER BY sortOrder ASC, updatedAt DESC")
    fun observeKnotsWithWords(): Flow<List<BorromeanKnotWithWords>>

    @Query("SELECT COUNT(*) FROM borromean_knots")
    suspend fun countKnots(): Int

    @Query("SELECT COUNT(*) FROM borromean_words WHERE knotId = :knotId AND registerType = :registerType")
    suspend fun countWords(knotId: String, registerType: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertKnot(knot: BorromeanKnotEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWord(word: BorromeanWordEntity)

    @Query("UPDATE borromean_knots SET title = :title, description = :description, updatedAt = :updatedAt WHERE id = :knotId")
    suspend fun updateKnot(knotId: String, title: String, description: String, updatedAt: Instant)

    @Query("UPDATE borromean_knots SET isArchived = 1, updatedAt = :updatedAt WHERE id = :knotId")
    suspend fun archiveKnot(knotId: String, updatedAt: Instant)

    @Query("UPDATE borromean_words SET text = :text, updatedAt = :updatedAt WHERE id = :wordId")
    suspend fun updateWord(wordId: String, text: String, updatedAt: Instant)

    @Query("UPDATE borromean_words SET conversationId = :conversationId, updatedAt = :updatedAt WHERE id = :wordId")
    suspend fun linkWordToConversation(wordId: String, conversationId: String, updatedAt: Instant)

    @Query("SELECT * FROM borromean_words WHERE id = :wordId")
    suspend fun getWord(wordId: String): BorromeanWordEntity?

    @Query("DELETE FROM borromean_words WHERE id = :wordId")
    suspend fun deleteWord(wordId: String)
}

@Dao
interface SafetyEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSafetyEvent(event: SafetyEventEntity)
}

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPayment(payment: PaymentEntity)
}
