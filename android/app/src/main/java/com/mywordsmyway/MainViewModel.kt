package com.mywordsmyway

import android.net.Uri
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mywordsmyway.data.local.BorromeanKnotWithWords
import com.mywordsmyway.data.local.BorromeanWordEntity
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.NoteFolderWithCount
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.BorromeanWordCalculationProgress
import com.mywordsmyway.data.model.BorromeanWordCalculationReport
import com.mywordsmyway.data.model.BorromeanWordCalculationUiState
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.StorageUsage
import com.mywordsmyway.data.repository.ConversationRepository
import com.mywordsmyway.data.repository.BorromeanCalculationStateRepository
import com.mywordsmyway.data.repository.StorageRepository
import com.mywordsmyway.data.repository.WordRepository
import com.mywordsmyway.model.ModelService
import com.mywordsmyway.model.GemmaModelDownloadProgress
import com.mywordsmyway.model.ModelSettings
import com.mywordsmyway.model.ModelSettingsRepository
import com.mywordsmyway.storage.AudioExportFile
import com.mywordsmyway.recorder.AndroidAudioRecorder
import com.mywordsmyway.storage.NotesExporter
import com.mywordsmyway.storage.TextExportFile
import com.mywordsmyway.words.BorromeanCalculationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

data class RecordUiState(
    val isRecording: Boolean = false,
    val isSaving: Boolean = false,
    val memoCount: Int = 0,
    val safetyMessage: String = "",
    val errorMessage: String = "",
)

class MainViewModel(
    private val appContext: Context,
    private val conversationRepository: ConversationRepository,
    private val wordRepository: WordRepository,
    private val storageRepository: StorageRepository,
    private val audioRecorder: AndroidAudioRecorder,
    private val notesExporter: NotesExporter,
    private val modelService: ModelService,
    private val modelSettingsRepository: ModelSettingsRepository,
    private val calculationStateRepository: BorromeanCalculationStateRepository,
) : ViewModel() {
    val storageUsage: StateFlow<StorageUsage> = storageRepository.observeStorageUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageUsage(0L, 1_073_741_824L))

    val nouns: StateFlow<List<NounWithLinksEntity>> = wordRepository.observeNouns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val borromeanKnots: StateFlow<List<BorromeanKnotWithWords>> = wordRepository.observeBorromeanKnots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val globalBorromeanWords: StateFlow<List<BorromeanWordEntity>> = wordRepository.observeGlobalBorromeanWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unprocessedGemmaNoteCount: StateFlow<Int> = conversationRepository.observeUnprocessedGemmaNoteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val modelSettings: StateFlow<ModelSettings> = modelSettingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelSettings())

    var recordUiState by mutableStateOf(RecordUiState())
        private set

    val gemmaDownloadProgress = MutableStateFlow(GemmaModelDownloadProgress.Idle)
    val borromeanCalculationProgress = MutableStateFlow(calculationStateRepository.loadProgress().let { progress ->
        if (progress.isActive) progress.copy(isActive = false, currentStep = "Previous calculation was interrupted.") else progress
    })
    val borromeanCalculationUiState = MutableStateFlow(calculationStateRepository.loadUiState().let { state ->
        if (state.isRunning) {
            state.copy(
                isRunning = false,
                message = "Previous Gemma calculation was interrupted.",
                logLines = state.logLines + "Previous calculation was interrupted before completion.",
                error = "The app or Android stopped the previous calculation before it completed.",
            )
        } else {
            state
        }
    })
    private var borromeanCalculationJob: Job? = null

    init {
        persistBorromeanCalculationProgress()
        persistBorromeanCalculationUiState()
    }

    private fun setBorromeanCalculationUiState(state: BorromeanWordCalculationUiState) {
        borromeanCalculationUiState.value = state
        persistBorromeanCalculationUiState()
    }

    private fun setBorromeanCalculationProgress(progress: BorromeanWordCalculationProgress) {
        borromeanCalculationProgress.value = progress
        persistBorromeanCalculationProgress()
    }

    private fun persistBorromeanCalculationUiState() {
        calculationStateRepository.saveUiState(borromeanCalculationUiState.value)
    }

    private fun persistBorromeanCalculationProgress() {
        calculationStateRepository.saveProgress(borromeanCalculationProgress.value)
    }

    override fun onCleared() {
        BorromeanCalculationService.stop(appContext)
        super.onCleared()
    }

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

    fun observeNoteFolders(): Flow<List<NoteFolderWithCount>> =
        conversationRepository.observeNoteFolders()

    fun observeNotes(folderId: String?, query: String, startDate: String, endDate: String): Flow<List<ConversationEntity>> =
        conversationRepository.observeNotes(folderId, query, startDate, endDate)

    fun observeNotesInFolder(folderId: String?, startDate: String, endDate: String): Flow<List<ConversationEntity>> =
        conversationRepository.observeNotesInFolder(folderId, startDate, endDate)

    suspend fun startConversation(): Result<String> = runCatching {
        conversationRepository.startConversation().conversation.id
    }

    suspend fun startNote(folderId: String? = null): Result<String> = runCatching {
        conversationRepository.startConversation(folderId = folderId).conversation.id
    }

    suspend fun createNoteFolder(name: String): Result<Unit> = runCatching {
        conversationRepository.createNoteFolder(name)
    }

    suspend fun renameNoteFolder(folderId: String, name: String): Result<Unit> = runCatching {
        conversationRepository.renameNoteFolder(folderId, name)
    }

    suspend fun deleteNoteFolder(folderId: String): Result<Unit> = runCatching {
        conversationRepository.deleteNoteFolder(folderId)
    }

    suspend fun moveNoteToFolder(conversationId: String, folderId: String?): Result<Unit> = runCatching {
        conversationRepository.moveNoteToFolder(conversationId, folderId)
    }

    suspend fun lockNote(conversationId: String, password: String): Result<Unit> = runCatching {
        conversationRepository.lockNote(conversationId, password)
    }

    suspend fun lockNoteWithDeviceAuth(conversationId: String): Result<Unit> = runCatching {
        conversationRepository.lockNoteWithDeviceAuth(conversationId)
    }

    suspend fun removeNoteLock(conversationId: String): Result<Unit> = runCatching {
        conversationRepository.removeNoteLock(conversationId)
    }

    suspend fun verifyNotePassword(conversationId: String, password: String): Result<Boolean> = runCatching {
        conversationRepository.verifyNotePassword(conversationId, password)
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

    suspend fun setGemmaModelPath(path: String): Result<Unit> = runCatching {
        modelSettingsRepository.setGemmaModelPath(path)
    }

    suspend fun importGemmaModel(sourceUri: Uri): Result<String> = runCatching {
        modelSettingsRepository.importGemmaModel(sourceUri)
    }

    suspend fun startGemmaModelDownload(): Result<Unit> = runCatching {
        val downloadId = modelSettingsRepository.startGemmaModelDownload()
        viewModelScope.launch {
            modelSettingsRepository.observeGemmaModelDownload(downloadId).collectLatest { progress ->
                gemmaDownloadProgress.value = progress
            }
        }
    }

    suspend fun gemmaModelStatus(): Result<String> = runCatching {
        modelService.modelStatus()
    }

    suspend fun extractBorromeanWords(conversationId: String, noteText: String, objectKnotId: String?): Result<Int> = runCatching {
        val source = conversationRepository.buildGemmaExtractionSource(conversationId, noteText)
        if (source.isBlank()) return@runCatching 0
        val existingWords = borromeanKnots.value.flatMap { knot -> knot.words.map { it.text } } +
            globalBorromeanWords.value.map { it.text }
        val result = modelService.extractBorromeanWords(source, existingWords)
        wordRepository.saveExtractedBorromeanWords(conversationId, objectKnotId, result.candidateWords)
    }

    fun startBorromeanWordsCalculation() {
        if (borromeanCalculationJob?.isActive == true || borromeanCalculationUiState.value.isRunning) return
        setBorromeanCalculationUiState(BorromeanWordCalculationUiState(
            isRunning = true,
            message = "Gemma is calculating words from saved notes and voice memos...",
            logLines = listOf("Started Gemma Words calculation."),
            error = "",
        ))
        BorromeanCalculationService.start(appContext)
            .onFailure { addBorromeanCalculationLog("Background keep-alive could not start: ${it.message ?: it::class.java.simpleName}") }
        borromeanCalculationJob = viewModelScope.launch {
            calculateBorromeanWordsWithGemma()
                .onSuccess { report ->
                    val count = report.insertedCount
                    setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(
                        isRunning = false,
                        message = if (count > 0) {
                            "Gemma added $count word${if (count == 1) "" else "s"}."
                        } else {
                            "Gemma did not add new words. See the calculation log below for the reason."
                        },
                        logLines = report.logLines,
                        error = "",
                    ))
                    BorromeanCalculationService.stop(appContext)
                }
                .onFailure { throwable ->
                    if (throwable.isCalculationCancellation()) {
                        finishCancelledBorromeanCalculation()
                        return@onFailure
                    }
                    val message = throwable.message ?: "Could not calculate words with Gemma."
                    addBorromeanCalculationLog("Calculation failed: $message")
                    setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(
                        isRunning = false,
                        message = "Gemma calculation failed.",
                        error = message,
                    ))
                    BorromeanCalculationService.stop(appContext)
                }
        }
    }

    private fun finishCancelledBorromeanCalculation() {
        BorromeanCalculationService.stop(appContext)
        setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(
            isRunning = false,
            message = "Gemma calculation stopped. Unprocessed notes can be calculated later.",
            error = "",
        ))
        setBorromeanCalculationProgress(borromeanCalculationProgress.value.copy(
            isActive = false,
            currentStep = "Stopped.",
            estimatedRemainingMillis = null,
        ))
    }

    fun stopBorromeanCalculationForBackground() {
        if (borromeanCalculationJob?.isActive != true && !borromeanCalculationUiState.value.isRunning) return
        borromeanCalculationJob?.cancel()
        borromeanCalculationJob = null
        BorromeanCalculationService.stop(appContext)
        addBorromeanCalculationLog("Calculation stopped because the app went to the background. Reopen Words and tap Calculate again to restart.")
        setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(
            isRunning = false,
            message = "Gemma calculation stopped to keep the phone responsive.",
            error = "Calculation stopped when the app went to the background.",
        ))
        setBorromeanCalculationProgress(borromeanCalculationProgress.value.copy(
            isActive = false,
            currentStep = "Stopped because app went to background.",
            estimatedRemainingMillis = null,
        ))
    }

    fun clearBorromeanCalculationLog() {
        if (borromeanCalculationUiState.value.isRunning) return
        calculationStateRepository.clear()
        setBorromeanCalculationUiState(BorromeanWordCalculationUiState())
        setBorromeanCalculationProgress(BorromeanWordCalculationProgress())
    }

    suspend fun calculateBorromeanWordsWithGemma(): Result<BorromeanWordCalculationReport> = runCatching {
        var inserted = 0
        val logs = mutableListOf<String>()
        val startedAt = TimeSource.Monotonic.markNow()
        var completedSteps = 0
        val status = modelService.modelStatus()
        val notes = conversationRepository.getUnprocessedGemmaNoteContent()
        val totalSteps = (notes.size * 3).coerceAtLeast(1)
        fun updateProgress(currentStep: String) {
            val elapsedMillis = startedAt.elapsedNow().inWholeMilliseconds
            val remainingMillis = if (completedSteps > 0 && completedSteps < totalSteps) {
                val averageStepMillis = elapsedMillis / completedSteps.toLong()
                averageStepMillis * (totalSteps - completedSteps)
            } else {
                null
            }
            setBorromeanCalculationProgress(BorromeanWordCalculationProgress(
                isActive = true,
                completedSteps = completedSteps,
                totalSteps = totalSteps,
                currentStep = currentStep,
                elapsedMillis = elapsedMillis,
                estimatedRemainingMillis = remainingMillis,
            ))
        }
        fun completeStep(nextStep: String) {
            completedSteps = (completedSteps + 1).coerceAtMost(totalSteps)
            updateProgress(nextStep)
        }
        updateProgress("Preparing Gemma calculation...")
        logs.addCalculationLog("Model: $status")
        logs.addCalculationLog("Found ${notes.size} new note${if (notes.size == 1) "" else "s"} to scan.")
        if (notes.isEmpty()) {
            logs.addCalculationLog("No new notes need Gemma. Add or edit a note/audio memo first.")
            completeStep("No notes to scan.")
        }
        notes.forEach { note ->
            val noteTitle = note.title.ifBlank { "Untitled note" }
            logs.addCalculationLog("Scanning \"$noteTitle\"...")
            val targetKnotId = borromeanKnots.value.firstOrNull()?.knot?.id
            updateProgress("Building source for \"$noteTitle\"...")
            val source = conversationRepository.buildGemmaExtractionSource(note.id, note.finalNote)
            completeStep("Source ready for \"$noteTitle\".")
            if (source.isBlank()) {
                logs.addCalculationLog("Skipped \"$noteTitle\": no note text or memo transcript was available.")
                logs.addCalculationLog("If this note only has audio, check that the Gemma model is ready so audio can be transcribed.")
                conversationRepository.markGemmaNoteProcessed(note.id)
                completeStep("Skipped Gemma extraction for \"$noteTitle\".")
                completeStep("Skipped saving for \"$noteTitle\".")
                return@forEach
            }
            logs.addCalculationLog("Built ${source.length} characters of source text.")
            val existingWords = borromeanKnots.value.flatMap { knot -> knot.words.map { it.text } } +
                globalBorromeanWords.value.map { it.text }
            updateProgress("Gemma is extracting words from \"$noteTitle\"...")
            val result = modelService.extractBorromeanWords(source, existingWords)
            completeStep("Gemma finished \"$noteTitle\".")
            if (result.backend.isNotBlank()) {
                logs.addCalculationLog("Gemma backend: ${result.backend}.")
            }
            if (result.diagnostic.isNotBlank()) {
                logs.addCalculationLog("Gemma detail: ${result.diagnostic}")
            }
            logs.addCalculationLog("Gemma returned ${result.candidateWords.size} candidate word${if (result.candidateWords.size == 1) "" else "s"}.")
            updateProgress("Saving words from \"$noteTitle\"...")
            val saved = runCatching {
                wordRepository.saveExtractedBorromeanWords(note.id, targetKnotId, result.candidateWords)
            }.getOrElse { throwable ->
                logs.addCalculationLog("Could not save words from \"$noteTitle\": ${throwable.message ?: throwable::class.java.simpleName}")
                0
            }
            completeStep("Saved words from \"$noteTitle\".")
            inserted += saved
            logs.addCalculationLog("Saved $saved new word${if (saved == 1) "" else "s"} from \"$noteTitle\".")
            if (result.candidateWords.isNotEmpty() && saved == 0) {
                logs.addCalculationLog("No new words were saved because candidates were duplicates, invalid, or already present.")
            }
            conversationRepository.markGemmaNoteProcessed(note.id)
            logs.addCalculationLog("Marked \"$noteTitle\" as processed.")
        }
        logs.addCalculationLog("Finished. Added $inserted new word${if (inserted == 1) "" else "s"}.")
        setBorromeanCalculationProgress(BorromeanWordCalculationProgress(
            isActive = false,
            completedSteps = totalSteps,
            totalSteps = totalSteps,
            currentStep = "Finished.",
            elapsedMillis = startedAt.elapsedNow().inWholeMilliseconds,
            estimatedRemainingMillis = 0L,
        ))
        BorromeanWordCalculationReport(insertedCount = inserted, logLines = logs)
    }.onFailure {
        val current = borromeanCalculationProgress.value
        setBorromeanCalculationProgress(current.copy(isActive = false, currentStep = "Calculation failed."))
    }

    private fun MutableList<String>.addCalculationLog(line: String) {
        add(line)
        setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(logLines = toList()))
    }

    private fun addBorromeanCalculationLog(line: String) {
        val logs = borromeanCalculationUiState.value.logLines + line
        setBorromeanCalculationUiState(borromeanCalculationUiState.value.copy(logLines = logs))
    }

    private fun Throwable.isCalculationCancellation(): Boolean =
        this is CancellationException ||
            message?.contains("StandaloneCoroutine was cancelled", ignoreCase = true) == true ||
            message?.contains("coroutine was cancelled", ignoreCase = true) == true

    suspend fun keepSuggestion(suggestionId: String): Result<Unit> = runCatching {
        wordRepository.keepSuggestion(suggestionId)
    }

    suspend fun renameSuggestion(suggestionId: String, newNoun: String): Result<Unit> = runCatching {
        wordRepository.renameSuggestion(suggestionId, newNoun)
    }

    suspend fun rejectSuggestion(suggestionId: String): Result<Unit> = runCatching {
        wordRepository.rejectSuggestion(suggestionId)
    }

    suspend fun createBorromeanKnot(title: String, description: String, personName: String? = null, theoryNote: String = ""): Result<String> = runCatching {
        wordRepository.createBorromeanKnot(title, description, personName, theoryNote)
    }

    suspend fun renameBorromeanKnot(knotId: String, title: String, description: String, personName: String? = null, theoryNote: String = ""): Result<Unit> = runCatching {
        wordRepository.renameBorromeanKnot(knotId, title, description, personName, theoryNote)
    }

    suspend fun archiveBorromeanKnot(knotId: String): Result<Unit> = runCatching {
        wordRepository.archiveBorromeanKnot(knotId)
    }

    suspend fun addBorromeanWord(
        knotId: String?,
        text: String,
        registerType: String,
        objectPartType: String? = null,
        emotionalWeight: Int = 50,
        importanceWeight: Int = 50,
        desireWeight: Int = 50,
    ): Result<String> = runCatching {
        wordRepository.addBorromeanWord(knotId, text, registerType, objectPartType, emotionalWeight, importanceWeight, desireWeight)
    }

    suspend fun renameBorromeanWord(wordId: String, text: String): Result<Unit> = runCatching {
        wordRepository.renameBorromeanWord(wordId, text)
    }

    suspend fun updateBorromeanWordDetails(
        wordId: String,
        text: String,
        objectPartType: String?,
        emotionalWeight: Int,
        importanceWeight: Int,
        desireWeight: Int,
    ): Result<Unit> = runCatching {
        wordRepository.updateBorromeanWordDetails(wordId, text, objectPartType, emotionalWeight, importanceWeight, desireWeight)
    }

    suspend fun updateBorromeanWordWeights(wordId: String, emotionalWeight: Int, importanceWeight: Int, desireWeight: Int): Result<Unit> = runCatching {
        wordRepository.updateBorromeanWordWeights(wordId, emotionalWeight, importanceWeight, desireWeight)
    }

    suspend fun deleteBorromeanWord(wordId: String): Result<Unit> = runCatching {
        wordRepository.deleteBorromeanWord(wordId)
    }

    suspend fun deleteAllBorromeanData(): Result<Unit> = runCatching {
        wordRepository.deleteAllBorromeanData()
        conversationRepository.clearGemmaProcessedNotes()
    }

    suspend fun linkBorromeanWordToConversation(wordId: String, conversationId: String): Result<Unit> = runCatching {
        wordRepository.linkBorromeanWordToConversation(wordId, conversationId)
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

    suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
        conversationRepository.deleteConversation(conversationId)
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

    suspend fun createNotesOnlyExport(): Result<TextExportFile> = runCatching {
        notesExporter.writeNotesOnlyExport()
    }

    suspend fun createWordsExport(): Result<TextExportFile> = runCatching {
        notesExporter.writeWordsExport()
    }

    suspend fun createNoteAudioExport(conversationId: String): Result<AudioExportFile> = runCatching {
        notesExporter.writeNoteAudioExport(conversationId)
    }

    suspend fun createAllAudioExport(): Result<AudioExportFile> = runCatching {
        notesExporter.writeAllAudioExport()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                appContext = container.appContext,
                conversationRepository = container.conversationRepository,
                wordRepository = container.wordRepository,
                storageRepository = container.storageRepository,
                audioRecorder = container.audioRecorder,
                notesExporter = container.notesExporter,
                modelService = container.modelService,
                modelSettingsRepository = container.modelSettingsRepository,
                calculationStateRepository = container.borromeanCalculationStateRepository,
            ) as T
        }
    }
}
