package com.mywordsmyway.data.repository

import android.net.Uri
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.NoteFolderWithCount
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.AddMemoResult
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.StartConversationResult
import com.mywordsmyway.data.model.WeeklyAccess
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeWeeklyAccess(): Flow<WeeklyAccess>
    fun observeCurrentConversation(): Flow<ConversationEntity?>
    fun observeConversationHistory(): Flow<List<ConversationSummaryEntity>>
    fun observeConversation(conversationId: String): Flow<ConversationEntity?>
    fun observeMemos(conversationId: String): Flow<List<VoiceMemoEntity>>
    fun observeNoteImages(conversationId: String): Flow<List<NoteImageEntity>>
    fun observeNoteFolders(): Flow<List<NoteFolderWithCount>>
    fun observeNotes(folderId: String?, query: String, startDate: String, endDate: String): Flow<List<ConversationEntity>>
    fun observeNotesInFolder(folderId: String?, startDate: String, endDate: String): Flow<List<ConversationEntity>>
    fun observeUnprocessedGemmaNoteCount(): Flow<Int>
    suspend fun getAllNoteContent(): List<ConversationEntity>
    suspend fun getUnprocessedGemmaNoteContent(): List<ConversationEntity>
    suspend fun markGemmaNoteProcessed(conversationId: String)
    suspend fun clearGemmaProcessedNotes()
    suspend fun startConversation(paymentAcknowledged: Boolean): StartConversationResult
    suspend fun startConversation(paymentAcknowledged: Boolean, folderId: String?): StartConversationResult
    suspend fun createNoteFolder(name: String)
    suspend fun renameNoteFolder(folderId: String, name: String)
    suspend fun deleteNoteFolder(folderId: String)
    suspend fun moveNoteToFolder(conversationId: String, folderId: String?)
    suspend fun lockNote(conversationId: String, password: String)
    suspend fun lockNoteWithDeviceAuth(conversationId: String)
    suspend fun removeNoteLock(conversationId: String)
    suspend fun verifyNotePassword(conversationId: String, password: String): Boolean
    suspend fun addMemo(
        conversationId: String,
        audioPath: String?,
        durationMillis: Long? = null,
        textFallback: String? = null,
    ): AddMemoResult
    suspend fun addNoteImage(conversationId: String, sourceUri: Uri): NoteImageEntity
    suspend fun addDrawingImage(conversationId: String, pngBytes: ByteArray): NoteImageEntity
    suspend fun addNoteFile(conversationId: String, sourceUri: Uri): NoteFileAttachment
    suspend fun deleteNoteImage(imageId: String)
    suspend fun updateNote(conversationId: String, title: String, finalNote: String)
    suspend fun finishConversation(conversationId: String, title: String, finalNote: String): List<NounSuggestionEntity>
    suspend fun buildGemmaExtractionSource(conversationId: String, noteText: String): String
    suspend fun deleteConversation(conversationId: String)
    suspend fun deleteAllConversations()
}
