package com.mywordsmyway

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.StorageUsage
import com.mywordsmyway.data.model.WeeklyAccess
import com.mywordsmyway.data.repository.ConversationRepository
import com.mywordsmyway.data.repository.StorageRepository
import com.mywordsmyway.data.repository.WordRepository
import com.mywordsmyway.recorder.AndroidAudioRecorder
import com.mywordsmyway.storage.NotesExporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class RecordUiState(
    val isRecording: Boolean = false,
    val isSaving: Boolean = false,
    val memoCount: Int = 0,
    val safetyMessage: String = "",
    val errorMessage: String = "",
)

class MainViewModel(
    private val conversationRepository: ConversationRepository,
    private val wordRepository: WordRepository,
    private val storageRepository: StorageRepository,
    private val audioRecorder: AndroidAudioRecorder,
    private val notesExporter: NotesExporter,
) : ViewModel() {
    val weeklyAccess: StateFlow<WeeklyAccess> = conversationRepository.observeWeeklyAccess()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyAccess(true))

    val storageUsage: StateFlow<StorageUsage> = storageRepository.observeStorageUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageUsage(0L, 1_073_741_824L))

    val nouns: StateFlow<List<NounWithLinksEntity>> = wordRepository.observeNouns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var recordUiState by mutableStateOf(RecordUiState())
        private set

    fun observeCurrentConversation(): Flow<ConversationEntity?> =
        conversationRepository.observeCurrentConversation()

    fun observeConversation(conversationId: String): Flow<ConversationEntity?> =
        conversationRepository.observeConversation(conversationId)

    fun observeConversationHistory(): Flow<List<ConversationSummaryEntity>> =
        conversationRepository.observeConversationHistory()

    fun observeMemos(conversationId: String): Flow<List<VoiceMemoEntity>> =
        conversationRepository.observeMemos(conversationId)

    fun observeNoteImages(conversationId: String): Flow<List<NoteImageEntity>> =
        conversationRepository.observeNoteImages(conversationId)

    fun observeSuggestions(conversationId: String): Flow<List<NounSuggestionEntity>> =
        wordRepository.observeSuggestions(conversationId)

    fun observeNotes(query: String, startDate: String, endDate: String): Flow<List<ConversationEntity>> =
        conversationRepository.observeNotes(query, startDate, endDate)

    suspend fun startConversation(paymentAcknowledged: Boolean): Result<String> = runCatching {
        conversationRepository.startConversation(paymentAcknowledged).conversation.id
    }

    suspend fun startNote(): Result<String> = runCatching {
        val needsAcknowledgement = !weeklyAccess.value.freeConversationAvailable
        conversationRepository.startConversation(paymentAcknowledged = needsAcknowledgement).conversation.id
    }

    fun startRecording(): Boolean {
        return runCatching {
            audioRecorder.start()
            recordUiState = recordUiState.copy(
                isRecording = true,
                isSaving = false,
                errorMessage = "",
            )
        }.fold(
            onSuccess = { true },
            onFailure = {
                recordUiState = recordUiState.copy(errorMessage = it.message ?: "Could not start recording.")
                false
            },
        )
    }

    suspend fun stopRecordingAndSave(conversationId: String): String? {
        val recordedAudio = audioRecorder.stop()
        if (recordedAudio == null) {
            recordUiState = recordUiState.copy(
                isRecording = false,
                isSaving = false,
                errorMessage = "The recording was too short to save.",
            )
            return null
        }
        recordUiState = recordUiState.copy(isRecording = false, isSaving = true, errorMessage = "")
        return runCatching {
            conversationRepository.addMemo(
                conversationId = conversationId,
                audioPath = recordedAudio.path,
                durationMillis = recordedAudio.durationMillis,
            )
        }.fold(
            onSuccess = { result ->
                recordUiState = recordUiState.copy(
                    isSaving = false,
                    memoCount = result.memoCount,
                    safetyMessage = result.safety.safeUserMessage,
                    errorMessage = "",
                )
                result.memo.id
            },
            onFailure = { error ->
                recordUiState = recordUiState.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not save memo.",
                )
                null
            },
        )
    }

    fun cancelRecording() {
        audioRecorder.cancel()
        recordUiState = recordUiState.copy(isRecording = false, isSaving = false)
    }

    suspend fun saveDemoMemo(conversationId: String): Result<Unit> = runCatching {
        val result = conversationRepository.addMemo(conversationId, audioPath = null, textFallback = null)
        recordUiState = recordUiState.copy(
            memoCount = result.memoCount,
            safetyMessage = result.safety.safeUserMessage,
            errorMessage = "",
        )
    }

    suspend fun deleteMemoAudio(memoId: String): Result<Unit> = runCatching {
        storageRepository.deleteMemoAudio(memoId)
    }

    suspend fun saveNoteImage(conversationId: String, sourceUri: Uri): Result<String> = runCatching {
        conversationRepository.addNoteImage(conversationId, sourceUri).id
    }

    suspend fun saveDrawingImage(conversationId: String, pngBytes: ByteArray): Result<String> = runCatching {
        conversationRepository.addDrawingImage(conversationId, pngBytes).id
    }

    suspend fun saveNoteFile(conversationId: String, sourceUri: Uri): Result<NoteFileAttachment> = runCatching {
        conversationRepository.addNoteFile(conversationId, sourceUri)
    }

    suspend fun deleteNoteImage(imageId: String): Result<Unit> = runCatching {
        conversationRepository.deleteNoteImage(imageId)
    }

    suspend fun updateNote(conversationId: String, title: String, note: String): Result<Unit> = runCatching {
        conversationRepository.updateNote(conversationId, title, note)
    }

    suspend fun finishConversation(conversationId: String, title: String, note: String): Result<Int> = runCatching {
        conversationRepository.finishConversation(conversationId, title, note).size
    }

    suspend fun keepSuggestion(suggestionId: String): Result<Unit> = runCatching {
        wordRepository.keepSuggestion(suggestionId)
    }

    suspend fun renameSuggestion(suggestionId: String, newNoun: String): Result<Unit> = runCatching {
        wordRepository.renameSuggestion(suggestionId, newNoun)
    }

    suspend fun rejectSuggestion(suggestionId: String): Result<Unit> = runCatching {
        wordRepository.rejectSuggestion(suggestionId)
    }

    suspend fun renameNoun(nounId: String, newNoun: String): Result<Unit> = runCatching {
        wordRepository.renameNoun(nounId, newNoun)
    }

    suspend fun mergeNoun(sourceNounId: String, targetNounId: String): Result<Unit> = runCatching {
        wordRepository.mergeNoun(sourceNounId, targetNounId)
    }

    suspend fun deleteNoun(nounId: String): Result<Unit> = runCatching {
        wordRepository.deleteNoun(nounId)
    }

    suspend fun deleteAllConversations(): Result<Unit> = runCatching {
        conversationRepository.deleteAllConversations()
    }

    suspend fun deleteAllNouns(): Result<Unit> = runCatching {
        wordRepository.deleteAllNouns()
    }

    suspend fun deletePendingSuggestions(): Result<Unit> = runCatching {
        wordRepository.deletePendingSuggestions()
    }

    suspend fun deleteOldAudioNow(): Result<Unit> = runCatching {
        storageRepository.deleteOldAudioNow()
    }

    suspend fun deleteAllHiddenTranscripts(): Result<Unit> = runCatching {
        storageRepository.deleteAllHiddenTranscripts()
    }

    suspend fun createNotesOnlyExport(): Result<Uri> = runCatching {
        notesExporter.writeNotesOnlyExport()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                conversationRepository = container.conversationRepository,
                wordRepository = container.wordRepository,
                storageRepository = container.storageRepository,
                audioRecorder = container.audioRecorder,
                notesExporter = container.notesExporter,
            ) as T
        }
    }
}
