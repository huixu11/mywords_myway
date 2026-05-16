package com.mywordsmyway.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.Activity
import android.app.KeyguardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Brush as ComposeBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.mywordsmyway.MainViewModel
import com.mywordsmyway.RecordUiState
import com.mywordsmyway.audio.AudioPlaybackController
import com.mywordsmyway.audio.AudioPlaybackUiState
import com.mywordsmyway.data.local.BorromeanKnotWithWords
import com.mywordsmyway.data.local.BorromeanWordEntity
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.NoteFolderWithCount
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NounEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.BorromeanWordCalculationProgress
import com.mywordsmyway.data.model.LISTENING_QUESTION
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.SUPPORT_MESSAGE
import com.mywordsmyway.model.GemmaModelDownloadProgress
import com.mywordsmyway.model.GEMMA_4_E2B_MODEL_NAME
import com.mywordsmyway.model.GEMMA_4_E2B_MODEL_PAGE_URL
import com.mywordsmyway.storage.AudioExportFile
import com.mywordsmyway.storage.TextExportFile
import com.mywordsmyway.storage.plainNoteText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale

@Composable
fun AccessScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    onConversationStarted: (String) -> Unit,
    onConversationSelected: (String) -> Unit,
    onNotNow: () -> Unit,
) {
    val history by viewModel.observeConversationHistory().collectAsState(initial = emptyList())
    val currentConversation by viewModel.observeCurrentConversation().collectAsState(initial = null)
    val visibleHistory = remember(history, currentConversation?.id) {
        history.filterNot { it.conversation.id == currentConversation?.id }
    }
    val scope = rememberCoroutineScope()
    var error by rememberSaveable { mutableStateOf("") }

    Page(contentPadding = contentPadding) {
        item {
            Text("Record", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("You can always create a note or start a voice reflection. Record multiple memos in one conversation. After each memo, the app asks one question. When you stop, write your own note.")
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        viewModel.startConversation()
                            .onSuccess(onConversationStarted)
                            .onFailure { error = it.message.orEmpty() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start conversation")
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "The app only asks: ${LISTENING_QUESTION}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ErrorText(error)
        }
        currentConversation?.let { conversation ->
            item {
                ActiveConversationRow(
                    conversation = conversation,
                    onContinue = { onConversationSelected(conversation.id) },
                )
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Past conversations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        if (visibleHistory.isEmpty()) {
            item {
                Text("No saved conversations yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(visibleHistory, key = { it.conversation.id }) { summary ->
                ConversationHistoryRow(
                    summary = summary,
                    onClick = {
                        if (summary.conversation.finalNote.isBlank()) {
                            onConversationSelected(summary.conversation.id)
                        } else {
                            onNotNow()
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun RecordScreen(
    viewModel: MainViewModel,
    conversationId: String,
    contentPadding: PaddingValues,
    onFinish: () -> Unit,
) {
    val memos by viewModel.observeMemos(conversationId).collectAsState(initial = emptyList())
    val recordState = viewModel.recordUiState
    val savedAudioMemos = remember(memos) { memos.filter { it.audioPath != null } }
    val totalDuration = remember(savedAudioMemos) { savedAudioMemos.sumOf { it.durationMillis ?: 0L } }
    val nextRound = memos.size + 1
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val playbackController = remember(context) { AudioPlaybackController(context, scope) }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasRecordPermission = granted
    }

    Page(contentPadding = contentPadding) {
        item {
            if (recordState.safetyMessage.isNotBlank()) {
                SupportPanel(recordState.safetyMessage)
                Spacer(Modifier.height(18.dp))
            }
            Text(
                "Conversation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${savedAudioMemos.size} saved audio ${if (savedAudioMemos.size == 1) "memo" else "memos"} - ${formatDuration(totalDuration)} recorded - 20-30 min recommended",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            Text("Round $nextRound", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (memos.isNotEmpty()) {
                QuestionPanel(label = "After memo ${memos.size}")
                Spacer(Modifier.height(10.dp))
            }
            Text(
                if (memos.isEmpty()) {
                    "Record memo 1."
                } else {
                    "Record memo $nextRound, or stop and write your note."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier
                        .size(190.dp)
                        .clickable(enabled = !recordState.isSaving) {
                            if (!hasRecordPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else if (recordState.isRecording) {
                                scope.launch { viewModel.stopRecordingAndSave(conversationId) }
                            } else {
                                viewModel.startRecording()
                            }
                        },
                    shape = CircleShape,
                    color = if (recordState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(38.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (recordState.isRecording) {
                                "Tap to stop memo $nextRound"
                            } else {
                                "Tap to record memo $nextRound"
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            if (!hasRecordPermission) {
                Text(
                    "Microphone permission is needed to save voice memos.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            ErrorText(recordState.errorMessage)
            if (recordState.isSaving) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(
                onClick = onFinish,
                enabled = memos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Stop and write note", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (memos.isNotEmpty()) {
            item {
                Text("Saved rounds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        itemsIndexed(memos, key = { _, memo -> memo.id }) { index, memo ->
            MemoRow(
                index = index + 1,
                memo = memo,
                playbackState = playbackController.state,
                onPlayPause = { playbackController.playOrPause(memo) },
                onSeek = playbackController::seekTo,
                onDeleteAudio = {
                    if (playbackController.state.memoId == memo.id) playbackController.stop()
                    scope.launch { viewModel.deleteMemoAudio(memo.id) }
                },
            )
            if (index < memos.lastIndex) {
                QuestionPanel(label = "After memo ${index + 1}")
            }
        }
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { playbackController.release() }
    }
}

@Composable
fun WriteNoteScreen(
    viewModel: MainViewModel,
    conversationId: String,
    contentPadding: PaddingValues,
    reviewAfterSave: Boolean = true,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val conversation by viewModel.observeConversation(conversationId).collectAsState(initial = null)
    val images by viewModel.observeNoteImages(conversationId).collectAsState(initial = emptyList())
    val memos by viewModel.observeMemos(conversationId).collectAsState(initial = emptyList())
    val recordState = viewModel.recordUiState
    val savedAudioMemos = remember(memos) { memos.filter { it.audioPath != null } }
    val totalDuration = remember(savedAudioMemos) { savedAudioMemos.sumOf { it.durationMillis ?: 0L } }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deviceAuthAvailable = remember(context) { context.deviceAuthAvailable() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val imeBottomPadding = with(density) { imeBottom.toDp() }
    val noteListState = rememberLazyListState()
    var titleFocused by remember { mutableStateOf(false) }
    var noteFocused by remember { mutableStateOf(false) }
    var formatMenuOpen by remember { mutableStateOf(false) }
    var attachmentMenuOpen by remember { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var noteField by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var noteUndoStack by remember { mutableStateOf(emptyList<NoteEditSnapshot>()) }
    var noteRedoStack by remember { mutableStateOf(emptyList<NoteEditSnapshot>()) }
    var suppressUndoCapture by remember { mutableStateOf(false) }
    var activeInlineFormatNames by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var loadedConversationId by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var savedNotice by rememberSaveable { mutableStateOf("") }
    var isAddingImage by rememberSaveable { mutableStateOf(false) }
    var isAddingFile by rememberSaveable { mutableStateOf(false) }
    var showDrawingPad by rememberSaveable { mutableStateOf(false) }
    var showAudioList by rememberSaveable { mutableStateOf(false) }
    var showFindInNote by rememberSaveable { mutableStateOf(false) }
    var gemmaNotice by rememberSaveable { mutableStateOf("") }
    var findQuery by rememberSaveable { mutableStateOf("") }
    var findReplaceMode by rememberSaveable { mutableStateOf(false) }
    var replaceQuery by rememberSaveable { mutableStateOf("") }
    var selectedFindIndex by rememberSaveable { mutableStateOf(0) }
    var showLockDialog by rememberSaveable { mutableStateOf(false) }
    var showUnlockDialog by rememberSaveable { mutableStateOf(false) }
    var sessionUnlocked by rememberSaveable(conversationId) { mutableStateOf(false) }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasRecordPermission = granted
    }
    var pendingDeviceAuthSuccess by remember { mutableStateOf<(() -> Unit)?>(null) }
    val playbackController = remember(context) { AudioPlaybackController(context, scope) }
    val deviceAuthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingDeviceAuthSuccess?.invoke()
        } else {
            error = "Authentication canceled."
        }
        pendingDeviceAuthSuccess = null
    }
    fun currentEditSnapshot(): NoteEditSnapshot = NoteEditSnapshot(title = title, note = noteField)
    fun updateEditState(nextTitle: String = title, nextNote: TextFieldValue = noteField) {
        if (!suppressUndoCapture && (nextTitle != title || nextNote.text != noteField.text)) {
            noteUndoStack = (noteUndoStack + currentEditSnapshot()).takeLast(60)
            noteRedoStack = emptyList()
        }
        title = nextTitle
        noteField = nextNote
        error = ""
        savedNotice = ""
    }
    fun undoNoteEdit() {
        val previous = noteUndoStack.lastOrNull() ?: return
        suppressUndoCapture = true
        noteUndoStack = noteUndoStack.dropLast(1)
        noteRedoStack = (noteRedoStack + currentEditSnapshot()).takeLast(60)
        title = previous.title
        noteField = previous.note
        error = ""
        savedNotice = ""
        suppressUndoCapture = false
    }
    fun redoNoteEdit() {
        val next = noteRedoStack.lastOrNull() ?: return
        suppressUndoCapture = true
        noteRedoStack = noteRedoStack.dropLast(1)
        noteUndoStack = (noteUndoStack + currentEditSnapshot()).takeLast(60)
        title = next.title
        noteField = next.note
        error = ""
        savedNotice = ""
        suppressUndoCapture = false
    }
    fun insertIntoNote(snippet: String) {
        updateEditState(nextNote = noteField.insertAtSelection(snippet))
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isAddingImage = true
            viewModel.saveNoteImage(conversationId, uri)
                .onSuccess { imageId -> insertIntoNote("\n[[image:$imageId]]\n") }
                .onFailure { error = it.message ?: "Could not add image." }
            isAddingImage = false
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isAddingFile = true
            viewModel.saveNoteFile(conversationId, uri)
                .onSuccess { attachment -> insertIntoNote("\n${fileAttachmentToken(attachment)}\n") }
                .onFailure { error = it.message ?: "Could not add file." }
            isAddingFile = false
        }
    }
    val supportMessage = remember(title, noteField.text, recordState.safetyMessage) {
        if (containsSelfHarmThought("$title\n${noteField.text}")) SUPPORT_MESSAGE else recordState.safetyMessage
    }
    val findMatches = remember(noteField.text, findQuery) { findVisibleMatchRanges(noteField.text, findQuery) }
    val noteLocked = conversation?.isLocked == true
    val noteHasCustomPassword = conversation?.passwordHash != null
    val contentHidden = noteLocked && !sessionUnlocked
    LaunchedEffect(findMatches.size) {
        selectedFindIndex = selectedFindIndex.coerceIn(0, (findMatches.size - 1).coerceAtLeast(0))
    }
    fun closeFindInNote() {
        showFindInNote = false
        findQuery = ""
        findReplaceMode = false
        replaceQuery = ""
        selectedFindIndex = 0
    }
    val writingMenuOpen = formatMenuOpen || attachmentMenuOpen
    val showWritingToolbar = !showFindInNote && (titleFocused || noteFocused || writingMenuOpen)
    LaunchedEffect(conversation?.id) {
        val loaded = conversation ?: return@LaunchedEffect
        if (loadedConversationId == loaded.id) return@LaunchedEffect
        suppressUndoCapture = true
        title = loaded.title.takeUnless { it == "Untitled reflection" || it == "Untitled note" }.orEmpty()
        noteField = TextFieldValue(loaded.finalNote, selection = TextRange(loaded.finalNote.length))
        noteUndoStack = emptyList()
        noteRedoStack = emptyList()
        loadedConversationId = loaded.id
        suppressUndoCapture = false
    }
    fun applyFormat(format: NoteFormat) {
        if (format.isInline && noteField.selection.collapsed) {
            activeInlineFormatNames = if (format.name in activeInlineFormatNames) {
                activeInlineFormatNames - format.name
            } else {
                activeInlineFormatNames + format.name
            }
        } else {
            updateEditState(nextNote = noteField.applyNoteFormat(format))
        }
        error = ""
        savedNotice = ""
    }
    fun startAudioAttachment() {
        attachmentMenuOpen = false
        focusManager.clearFocus()
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else if (recordState.isRecording) {
            scope.launch {
                viewModel.stopRecordingAndSave(conversationId)?.let { memoId ->
                    insertIntoNote("\n[[audio:$memoId]]\n")
                }
            }
        } else {
            viewModel.startRecording()
        }
    }
    fun saveNote(navigateAfterSave: Boolean, allowEmptyBack: Boolean = false) {
        if (contentHidden) {
            if (navigateAfterSave || allowEmptyBack) onBack()
            return
        }
        if (allowEmptyBack && title.isBlank() && noteField.text.isBlank() && memos.isEmpty() && images.isEmpty()) {
            onBack()
            return
        }
        scope.launch {
            val result = if (reviewAfterSave) {
                viewModel.finishConversation(conversationId, title, noteField.text).map { }
            } else {
                viewModel.updateNote(conversationId, title, noteField.text)
            }
            result
                .onSuccess {
                    error = ""
                    savedNotice = "Saved"
                    gemmaNotice = ""
                    if (navigateAfterSave) onSaved()
                }
                .onFailure {
                    if (allowEmptyBack && title.isBlank() && noteField.text.isBlank() && memos.isEmpty() && images.isEmpty()) {
                        onBack()
                    } else {
                        error = it.message ?: "Could not save note."
                    }
                }
        }
    }
    fun authenticateWithDevice(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onUnavailable: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        val credentialIntent = keyguardManager?.createConfirmDeviceCredentialIntent(title, subtitle)
        if (!deviceAuthAvailable || credentialIntent == null) {
            onUnavailable()
            return
        }
        pendingDeviceAuthSuccess = onSuccess
        runCatching { deviceAuthLauncher.launch(credentialIntent) }
            .onFailure {
                pendingDeviceAuthSuccess = null
                onError(it.message ?: "Could not open device authentication.")
            }
    }
    BackHandler {
        saveNote(navigateAfterSave = true, allowEmptyBack = true)
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { playbackController.release() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bottomSafePadding = contentPadding.calculateBottomPadding()
        val floatingControlsPadding = if (imeBottom > 0) imeBottomPadding + 72.dp else bottomSafePadding + 96.dp
        val visibleNoteEditorMinHeight = (maxHeight - 180.dp + floatingControlsPadding).coerceAtLeast(360.dp)
        LazyColumn(
            state = noteListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = floatingControlsPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(contentPadding.calculateTopPadding() + 96.dp))
                if (contentHidden) {
                    LockedNoteContent(
                        title = conversation?.title.orEmpty(),
                        createdAt = conversation?.createdAt,
                        deviceAuthAvailable = deviceAuthAvailable,
                        hasCustomPassword = noteHasCustomPassword,
                        onViewNote = {
                            authenticateWithDevice(
                                title = "Unlock Note",
                                subtitle = "Use your phone lock or fingerprint.",
                                onSuccess = {
                                    sessionUnlocked = true
                                    error = ""
                                },
                                onUnavailable = {
                                    if (noteHasCustomPassword) {
                                        showUnlockDialog = true
                                    } else {
                                        error = "Set up a phone screen lock or fingerprint to unlock this note."
                                    }
                                },
                                onError = { message -> error = message },
                            )
                        },
                        onEnterPassword = { showUnlockDialog = true },
                    )
                    ErrorText(error)
                    return@item
                }
                if (savedNotice.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(savedNotice, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
                if (gemmaNotice.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(gemmaNotice, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))
                QuestionPanel()
                if (supportMessage.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    SupportPanel(supportMessage)
                }
                Spacer(Modifier.height(18.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { updateEditState(nextTitle = it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            titleFocused = it.isFocused
                            if (it.isFocused && showFindInNote) closeFindInNote()
                        },
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isBlank()) {
                                Text(
                                    "Title",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.height(18.dp))
                NoteDocumentEditor(
                    value = noteField,
                    onValueChange = { updateEditState(nextNote = it) },
                    memos = memos,
                    images = images,
                    activeInlineFormats = activeInlineFormatNames.mapNotNull { runCatching { NoteFormat.valueOf(it) }.getOrNull() }.toSet(),
                    findQuery = findQuery.takeIf { showFindInNote }.orEmpty(),
                    selectedFindRange = findMatches.getOrNull(selectedFindIndex),
                    onFocusChange = {
                        noteFocused = it
                        if (it && showFindInNote) closeFindInNote()
                    },
                    onEditorTap = {
                        if (showFindInNote) closeFindInNote()
                    },
                    onDeleteAudio = { memoId -> scope.launch { viewModel.deleteMemoAudio(memoId) } },
                    playbackState = playbackController.state,
                    onPlayPauseMemo = { memo -> playbackController.playOrPause(memo) },
                    onSeekMemo = playbackController::seekTo,
                    onDeleteImage = { imageId -> scope.launch { viewModel.deleteNoteImage(imageId) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = visibleNoteEditorMinHeight),
                )
                ErrorText(error)
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            NoteTopBar(
                isLocked = noteLocked,
                contentVisible = !contentHidden,
                canUndo = noteUndoStack.isNotEmpty(),
                canRedo = noteRedoStack.isNotEmpty(),
                onBack = { saveNote(navigateAfterSave = true, allowEmptyBack = true) },
                onUndo = ::undoNoteEdit,
                onRedo = ::redoNoteEdit,
                onShare = { shareCurrentNote(context, title, noteField.text, images.size, savedAudioMemos.size) },
                onExportAudio = {
                    scope.launch {
                        viewModel.createNoteAudioExport(conversationId)
                            .onSuccess { shareAudioExport(context, it, "Export note audio") }
                            .onFailure { error = it.message ?: "Could not export note audio." }
                    }
                },
                canExportAudio = savedAudioMemos.isNotEmpty(),
                onFind = {
                    focusManager.clearFocus()
                    showFindInNote = true
                },
                onLock = { showLockDialog = true },
                onRemoveLock = {
                    scope.launch {
                        viewModel.removeNoteLock(conversationId)
                            .onSuccess {
                                sessionUnlocked = false
                                savedNotice = "Lock removed"
                            }
                            .onFailure { error = it.message ?: "Could not remove lock." }
                    }
                },
                onSave = { saveNote(navigateAfterSave = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = contentPadding.calculateTopPadding() + 8.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 8.dp,
                    ),
            )
        }

        if (!contentHidden && showFindInNote) {
            FindInNoteToolbar(
                query = findQuery,
                replaceQuery = replaceQuery,
                replaceMode = findReplaceMode,
                matchCount = findMatches.size,
                selectedIndex = selectedFindIndex,
                onReplaceModeChange = { findReplaceMode = it },
                onQueryChange = {
                    findQuery = it
                    selectedFindIndex = 0
                },
                onReplaceQueryChange = { replaceQuery = it },
                onReplaceCurrent = {
                    findMatches.getOrNull(selectedFindIndex)?.let { match ->
                        updateEditState(
                            nextNote = noteField.replaceTextRange(
                                start = match.first,
                                end = match.last + 1,
                                replacement = replaceQuery,
                                selectionStart = match.first + replaceQuery.length,
                            ),
                        )
                    }
                },
                onReplaceAll = {
                    if (findMatches.isNotEmpty()) {
                        val nextText = findMatches.asReversed().fold(noteField.text) { text, match ->
                            text.replaceRange(match.first, match.last + 1, replaceQuery)
                        }
                        updateEditState(nextNote = noteField.copy(text = nextText, selection = TextRange(nextText.length)))
                        selectedFindIndex = 0
                    }
                },
                onPrevious = {
                    if (findMatches.isNotEmpty()) {
                        val nextIndex = if (selectedFindIndex == 0) findMatches.lastIndex else selectedFindIndex - 1
                        selectedFindIndex = nextIndex
                        findMatches[nextIndex].let { match -> noteField = noteField.copy(selection = TextRange(match.first, match.last + 1)) }
                    }
                },
                onNext = {
                    if (findMatches.isNotEmpty()) {
                        val nextIndex = if (selectedFindIndex >= findMatches.lastIndex) 0 else selectedFindIndex + 1
                        selectedFindIndex = nextIndex
                        findMatches[nextIndex].let { match -> noteField = noteField.copy(selection = TextRange(match.first, match.last + 1)) }
                    }
                },
                onClose = {
                    closeFindInNote()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 8.dp,
                    ),
            )
        } else if (!contentHidden && showWritingToolbar) {
            WritingAccessoryToolbar(
                isAddingImage = isAddingImage,
                formatMenuOpen = formatMenuOpen,
                attachmentMenuOpen = attachmentMenuOpen,
                onFormatMenuChange = { formatMenuOpen = it },
                onAttachmentMenuChange = { attachmentMenuOpen = it },
                onFormat = ::applyFormat,
                activeInlineFormats = activeInlineFormatNames.mapNotNull { runCatching { NoteFormat.valueOf(it) }.getOrNull() }.toSet(),
                onChecklist = { insertIntoNote("\n[[check:0:Checklist item]]\n") },
                onTable = { insertIntoNote("\n[[table:Topic|Note;;|]]\n\n") },
                onAudio = ::startAudioAttachment,
                isAddingFile = isAddingFile,
                onAddImage = { imagePicker.launch("image/*") },
                onAddFile = { filePicker.launch("*/*") },
                onDraw = { showDrawingPad = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (titleFocused || noteFocused) 8.dp else contentPadding.calculateBottomPadding() + 12.dp,
                    ),
            )
        } else if (!contentHidden) {
            FloatingRecorder(
                recordState = recordState,
                memoCount = savedAudioMemos.size,
                totalDuration = totalDuration,
                hasRecordPermission = hasRecordPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onStart = { viewModel.startRecording() },
                onStop = { scope.launch { viewModel.stopRecordingAndSave(conversationId) } },
                onCancel = { viewModel.cancelRecording() },
                onOpenAudioList = { showAudioList = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 12.dp,
                    ),
            )
        }
    }

    if (showAudioList) {
        AudioMemoListSheet(
            memos = savedAudioMemos,
            playbackState = playbackController.state,
            onDismiss = { showAudioList = false },
            onPlayPause = { memo -> playbackController.playOrPause(memo) },
            onSeek = playbackController::seekTo,
            onDeleteAudio = { memo ->
                if (playbackController.state.memoId == memo.id) playbackController.stop()
                scope.launch { viewModel.deleteMemoAudio(memo.id) }
            },
        )
    }

    if (showLockDialog) {
        PasswordLockDialog(
            deviceAuthAvailable = deviceAuthAvailable,
            onDismiss = { showLockDialog = false },
            onUseDeviceAuth = {
                authenticateWithDevice(
                    title = "Lock Note",
                    subtitle = "Use your phone lock or fingerprint to protect this note.",
                    onSuccess = {
                        scope.launch {
                            viewModel.lockNoteWithDeviceAuth(conversationId)
                                .onSuccess {
                                    sessionUnlocked = true
                                    savedNotice = "Locked with phone lock"
                                    showLockDialog = false
                                }
                                .onFailure { error = it.message ?: "Could not lock note." }
                        }
                    },
                    onUnavailable = { error = "Set up a phone screen lock or fingerprint to use this lock method." },
                    onError = { message -> error = message },
                )
            },
            onLock = { password ->
                scope.launch {
                    viewModel.lockNote(conversationId, password)
                        .onSuccess {
                            sessionUnlocked = true
                            savedNotice = "Locked"
                            showLockDialog = false
                        }
                        .onFailure { error = it.message ?: "Could not lock note." }
                }
            },
        )
    }

    if (showUnlockDialog) {
        PasswordUnlockDialog(
            hasCustomPassword = noteHasCustomPassword,
            onDismiss = { showUnlockDialog = false },
            onUseDeviceAuth = {
                authenticateWithDevice(
                    title = "Unlock Note",
                    subtitle = "Use your phone lock or fingerprint.",
                    onSuccess = {
                        sessionUnlocked = true
                        showUnlockDialog = false
                        error = ""
                    },
                    onUnavailable = { error = "Set up a phone screen lock or fingerprint to unlock this note." },
                    onError = { message -> error = message },
                )
            },
            onUnlock = { password, onInvalid ->
                scope.launch {
                    viewModel.verifyNotePassword(conversationId, password)
                        .onSuccess { verified ->
                            if (verified) {
                                sessionUnlocked = true
                                showUnlockDialog = false
                                error = ""
                            } else {
                                onInvalid("Incorrect password.")
                            }
                        }
                        .onFailure { onInvalid(it.message ?: "Could not verify password.") }
                }
            },
        )
    }

    if (showDrawingPad) {
        DrawingPadDialog(
            onDismiss = { showDrawingPad = false },
            onSave = { pngBytes ->
                showDrawingPad = false
                scope.launch {
                    viewModel.saveDrawingImage(conversationId, pngBytes)
                        .onSuccess { imageId -> insertIntoNote("\n[[drawing:$imageId]]\n") }
                        .onFailure { error = it.message ?: "Could not save drawing." }
                }
            },
        )
    }
}

@Composable
fun ReviewScreen(
    viewModel: MainViewModel,
    conversationId: String,
    contentPadding: PaddingValues,
    onDone: () -> Unit,
) {
    val suggestions by viewModel.observeSuggestions(conversationId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var renameTarget by remember { mutableStateOf<NounSuggestionEntity?>(null) }
    var renameText by rememberSaveable { mutableStateOf("") }

    Page(contentPadding = contentPadding) {
        item {
            Text("Review possible words", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "The app found nouns in your note. If you keep a word, it will connect to the note you wrote.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            if (suggestions.isEmpty()) {
                Text("No pending suggestions.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Go to My Words")
                }
            }
        }
        items(suggestions, key = { it.id }) { suggestion ->
            SuggestionRow(
                suggestion = suggestion,
                onKeep = { scope.launch { viewModel.keepSuggestion(suggestion.id) } },
                onRename = {
                    renameTarget = suggestion
                    renameText = suggestion.suggestedNoun
                },
                onDelete = { scope.launch { viewModel.rejectSuggestion(suggestion.id) } },
            )
        }
        item {
            if (suggestions.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            }
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename word") },
            text = {
                Column {
                    Text("Current: ${target.suggestedNoun}")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        label = { Text("New word") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { viewModel.renameSuggestion(target.id, renameText) }
                        renameTarget = null
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun WordsScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    onOpenNote: (String) -> Unit,
    onCreateLinkedNote: (String) -> Unit,
) {
    val knots by viewModel.borromeanKnots.collectAsState()
    val globalWords by viewModel.globalBorromeanWords.collectAsState()
    val calculationProgress by viewModel.borromeanCalculationProgress.collectAsState()
    val calculationUiState by viewModel.borromeanCalculationUiState.collectAsState()
    val unprocessedGemmaNoteCount by viewModel.unprocessedGemmaNoteCount.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedKnotId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRegister by rememberSaveable { mutableStateOf(BORROMEAN_REGISTER_REAL) }
    var editingWord by remember { mutableStateOf<BorromeanWordEntity?>(null) }
    var addingRegister by rememberSaveable { mutableStateOf<String?>(null) }
    var wordText by rememberSaveable { mutableStateOf("") }
    var objectPartType by rememberSaveable { mutableStateOf("") }
    var emotionalWeight by rememberSaveable { mutableStateOf(50) }
    var importanceWeight by rememberSaveable { mutableStateOf(50) }
    var desireWeight by rememberSaveable { mutableStateOf(50) }
    var knotRotation by rememberSaveable { mutableStateOf(0f) }
    var knotStretch by rememberSaveable { mutableStateOf(1f) }
    var connectingWordId by rememberSaveable { mutableStateOf<String?>(null) }
    var connectingWordText by rememberSaveable { mutableStateOf("") }
    var connectingConversationId by rememberSaveable { mutableStateOf<String?>(null) }
    var noteSearchQuery by rememberSaveable { mutableStateOf("") }
    var knotPendingDelete by remember { mutableStateOf<BorromeanKnotWithWords?>(null) }
    var wordPendingDelete by remember { mutableStateOf<BorromeanWordEntity?>(null) }
    var showDeleteWordsDialog by rememberSaveable { mutableStateOf(false) }
    var showClearCalculationLogDialog by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }
    val activeKnot = remember(knots, selectedKnotId) {
        knots.firstOrNull { it.knot.id == selectedKnotId } ?: knots.firstOrNull()
    }
    val otherKnots = remember(knots, activeKnot?.knot?.id) {
        knots.filter { it.knot.id != activeKnot?.knot?.id }
    }
    LaunchedEffect(activeKnot?.knot?.id) {
        if (activeKnot != null) selectedKnotId = activeKnot.knot.id
    }
    val previousKnotIds = remember { mutableStateListOf<String>() }
    fun selectKnot(knotId: String) {
        selectedKnotId?.takeIf { it != knotId }?.let { previousKnotIds.add(it) }
        selectedKnotId = knotId
        selectedRegister = BORROMEAN_REGISTER_REAL
    }
    fun openWordEditor(word: BorromeanWordEntity?, registerType: String) {
        editingWord = word
        addingRegister = registerType
        wordText = word?.text.orEmpty()
        objectPartType = word?.objectPartType.orEmpty()
        emotionalWeight = word?.emotionalWeight ?: if (registerType == BORROMEAN_REGISTER_AFFECT) 70 else 50
        importanceWeight = word?.importanceWeight ?: 50
        desireWeight = word?.desireWeight ?: if (registerType == BORROMEAN_REGISTER_DESIRE) 70 else 50
        error = ""
    }
    fun openNoteConnector(wordId: String, wordText: String, conversationId: String?) {
        connectingWordId = wordId
        connectingWordText = wordText
        connectingConversationId = conversationId
        noteSearchQuery = wordText
        error = ""
    }
    fun closeNoteConnector() {
        connectingWordId = null
        connectingWordText = ""
        connectingConversationId = null
        noteSearchQuery = ""
    }
    fun saveWord() {
        val register = addingRegister ?: BORROMEAN_REGISTER_OBJECT_A
        val knotId = if (register == BORROMEAN_REGISTER_AFFECT || register == BORROMEAN_REGISTER_DESIRE) {
            null
        } else {
            activeKnot?.knot?.id ?: return
        }
        val savedText = wordText
        scope.launch {
            if (editingWord == null) {
                viewModel.addBorromeanWord(
                    knotId = knotId,
                    text = savedText,
                    registerType = register,
                    objectPartType = objectPartType,
                    emotionalWeight = emotionalWeight,
                    importanceWeight = importanceWeight,
                    desireWeight = desireWeight,
                )
                    .onSuccess { wordId ->
                        editingWord = null
                        addingRegister = null
                        wordText = ""
                        objectPartType = ""
                        emotionalWeight = 50
                        importanceWeight = 50
                        desireWeight = 50
                        error = ""
                        openNoteConnector(wordId, savedText, null)
                    }
                    .onFailure { error = it.message ?: "Could not save word." }
            } else {
                val word = requireNotNull(editingWord)
                viewModel.updateBorromeanWordDetails(
                    wordId = word.id,
                    text = savedText,
                    objectPartType = objectPartType,
                    emotionalWeight = emotionalWeight,
                    importanceWeight = importanceWeight,
                    desireWeight = desireWeight,
                )
                    .onSuccess {
                        editingWord = null
                        addingRegister = null
                        wordText = ""
                        objectPartType = ""
                        emotionalWeight = 50
                        importanceWeight = 50
                        desireWeight = 50
                        error = ""
                    }
                    .onFailure { error = it.message ?: "Could not save word." }
            }
        }
    }

    Page(contentPadding = contentPadding) {
        item {
            Text("Borromean Knot", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "The topological mind model introduced by Jacques Lacan.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    error = ""
                    viewModel.startBorromeanWordsCalculation()
                },
                enabled = !calculationUiState.isRunning && unprocessedGemmaNoteCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        calculationUiState.isRunning -> "Calculating with Gemma..."
                        unprocessedGemmaNoteCount > 0 -> "Calculate $unprocessedGemmaNoteCount unprocessed note${if (unprocessedGemmaNoteCount == 1) "" else "s"} with Gemma"
                        else -> "All notes processed by Gemma"
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeleteWordsDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Delete Words data")
            }
            if (calculationUiState.message.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(calculationUiState.message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (calculationUiState.error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                ErrorText(calculationUiState.error)
            }
            if (calculationProgress.isActive || calculationProgress.totalSteps > 0) {
                Spacer(Modifier.height(8.dp))
                GemmaCalculationProgressBar(progress = calculationProgress)
            }
            if (calculationUiState.logLines.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                if (calculationUiState.isRunning) {
                    GemmaCalculationLog(logLines = calculationUiState.logLines)
                } else {
                    SwipeToDeleteRow(
                        contentDescription = "Delete calculation log",
                        shape = RoundedCornerShape(12.dp),
                        onDelete = { showClearCalculationLogDialog = true },
                    ) {
                        GemmaCalculationLog(logLines = calculationUiState.logLines)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            if (activeKnot == null) {
                Text("No Gemma-extracted Borromean knots yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Set up the Gemma 4 E2B model in Privacy, then save notes or voice memos. Gemma will extract object a words and create knots here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.createBorromeanKnot(
                                title = "New Borromean knot",
                                description = "",
                                theoryNote = "The knot can stretch or rotate, but the topology remains.",
                            ).onSuccess { selectedKnotId = it }
                                .onFailure { error = it.message ?: "Could not create knot." }
                        }
                    },
                ) {
                    Text("Create knot manually")
                }
                return@item
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(activeKnot.knot.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "A Borromean knot has generated because the person has something that lets you have familiar feelings that you had when you were little with your mom.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            BorromeanObjectAHero(
                viewModel = viewModel,
                knot = activeKnot,
                selectedRegister = selectedRegister,
                rotationOffset = knotRotation,
                stretchFactor = knotStretch,
                onRegisterSelected = { selectedRegister = it },
                onKnotTransform = { rotationDelta, stretchDelta ->
                    knotRotation = (knotRotation + rotationDelta).coerceIn(-48f, 48f)
                    knotStretch = (knotStretch * stretchDelta).coerceIn(0.72f, 1.32f)
                },
                onEditWord = { word -> openWordEditor(word, BORROMEAN_REGISTER_OBJECT_A) },
                onAddObjectWord = { openWordEditor(null, BORROMEAN_REGISTER_OBJECT_A) },
                onDeleteObjectWord = { word -> wordPendingDelete = word },
                onConnectWordNote = { word -> openNoteConnector(word.id, word.text, word.conversationId) },
                onOpenNote = onOpenNote,
            )
            Spacer(Modifier.height(18.dp))
            ErrorText(error)
            if (otherKnots.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Other possible knots", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (previousKnotIds.isNotEmpty()) {
                        TextButton(onClick = { selectedKnotId = previousKnotIds.removeAt(previousKnotIds.lastIndex) }) {
                            Text("Previous")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        items(otherKnots, key = { it.knot.id }) { knot ->
            SwipeToDeleteRow(
                contentDescription = "Delete knot",
                shape = RoundedCornerShape(20.dp),
                onDelete = { knotPendingDelete = knot },
            ) {
                ObjectAKnotCard(
                    knot = knot,
                    onClick = { selectKnot(knot.knot.id) },
                )
            }
        }
        item {
            Spacer(Modifier.height(18.dp))
            WeightedBorromeanWordSection(
                viewModel = viewModel,
                title = "Personally important words",
                description = "These words are important because they come from our mom, also because we love our mom. We want to help you neutralize these words, so that they will not restrict you from seeing the whole picture of what happened to you, so that you can live freely and can follow your heart, because your words have power.",
                words = globalWords
                    .filter { it.registerType == BORROMEAN_REGISTER_AFFECT }
                    .sortedWith(compareByDescending<BorromeanWordEntity> { it.importanceWeight }.thenByDescending { it.emotionalWeight }),
                primaryLabel = "Importance",
                secondaryLabel = "Emotional attachment",
                primaryValue = { it.importanceWeight },
                secondaryValue = { it.emotionalWeight },
                onAddWord = { openWordEditor(null, BORROMEAN_REGISTER_AFFECT) },
                onEditWord = { openWordEditor(it, BORROMEAN_REGISTER_AFFECT) },
                onDeleteWord = { word -> wordPendingDelete = word },
                onConnectNote = { word -> openNoteConnector(word.id, word.text, word.conversationId) },
                onOpenNote = onOpenNote,
                onPrimaryChange = { word, value ->
                    scope.launch { viewModel.updateBorromeanWordWeights(word.id, word.emotionalWeight, value, word.desireWeight) }
                },
                onSecondaryChange = { word, value ->
                    scope.launch { viewModel.updateBorromeanWordWeights(word.id, value, word.importanceWeight, word.desireWeight) }
                },
            )
            Spacer(Modifier.height(18.dp))
            WeightedBorromeanWordSection(
                viewModel = viewModel,
                title = "What I truly want",
                description = "These words are important because they come from our mom, also because we love our mom. Our words have power, so use them wisely.",
                words = globalWords
                    .filter { it.registerType == BORROMEAN_REGISTER_DESIRE }
                    .sortedByDescending { it.desireWeight },
                primaryLabel = "Desire",
                secondaryLabel = "Importance",
                primaryValue = { it.desireWeight },
                secondaryValue = { it.importanceWeight },
                onAddWord = { openWordEditor(null, BORROMEAN_REGISTER_DESIRE) },
                onEditWord = { openWordEditor(it, BORROMEAN_REGISTER_DESIRE) },
                onDeleteWord = { word -> wordPendingDelete = word },
                onConnectNote = { word -> openNoteConnector(word.id, word.text, word.conversationId) },
                onOpenNote = onOpenNote,
                onPrimaryChange = { word, value ->
                    scope.launch { viewModel.updateBorromeanWordWeights(word.id, word.emotionalWeight, word.importanceWeight, value) }
                },
                onSecondaryChange = { word, value ->
                    scope.launch { viewModel.updateBorromeanWordWeights(word.id, word.emotionalWeight, value, word.desireWeight) }
                },
            )
        }
    }

    if (editingWord != null || addingRegister != null) {
        BorromeanWordDialog(
            title = if (editingWord == null) "Add word" else "Edit word",
            registerType = addingRegister ?: BORROMEAN_REGISTER_OBJECT_A,
            text = wordText,
            objectPartType = objectPartType,
            emotionalWeight = emotionalWeight,
            importanceWeight = importanceWeight,
            desireWeight = desireWeight,
            onTextChange = { wordText = it },
            onObjectPartTypeChange = { objectPartType = it },
            onEmotionalWeightChange = { emotionalWeight = it },
            onImportanceWeightChange = { importanceWeight = it },
            onDesireWeightChange = { desireWeight = it },
            onDismiss = {
                editingWord = null
                addingRegister = null
                wordText = ""
                objectPartType = ""
            },
            onSave = ::saveWord,
        )
    }
    val targetWordId = connectingWordId
    if (targetWordId != null) {
        BorromeanNoteConnectionSheet(
            viewModel = viewModel,
            wordText = connectingWordText,
            linkedConversationId = connectingConversationId,
            query = noteSearchQuery,
            onQueryChange = { noteSearchQuery = it },
            onDismiss = ::closeNoteConnector,
            onConnectExisting = { conversationId ->
                scope.launch {
                    viewModel.linkBorromeanWordToConversation(targetWordId, conversationId)
                        .onSuccess { closeNoteConnector() }
                        .onFailure { error = it.message ?: "Could not connect note." }
                }
            },
            onOpenNote = { conversationId ->
                closeNoteConnector()
                onOpenNote(conversationId)
            },
            onCreateNew = {
                closeNoteConnector()
                onCreateLinkedNote(targetWordId)
            },
        )
    }
    knotPendingDelete?.let { knot ->
        DeleteKnotConfirmationDialog(
            knot = knot,
            onDismiss = { knotPendingDelete = null },
            onDelete = {
                scope.launch {
                    viewModel.archiveBorromeanKnot(knot.knot.id)
                        .onSuccess {
                            knotPendingDelete = null
                            if (selectedKnotId == knot.knot.id) {
                                selectedKnotId = null
                                selectedRegister = BORROMEAN_REGISTER_REAL
                            }
                        }
                        .onFailure { error = it.message ?: "Could not delete knot." }
                }
            },
        )
    }
    wordPendingDelete?.let { word ->
        DeleteWordConfirmationDialog(
            word = word,
            onDismiss = { wordPendingDelete = null },
            onDelete = {
                scope.launch {
                    viewModel.deleteBorromeanWord(word.id)
                        .onSuccess { wordPendingDelete = null }
                        .onFailure { error = it.message ?: "Could not delete word." }
                }
            },
        )
    }
    if (showDeleteWordsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWordsDialog = false },
            title = { Text("Delete Words data?") },
            text = {
                Text(
                    "This deletes all Borromean knots and all Words-page word lists from this device. Notes, audio, and images are kept. Gemma processing history is also cleared, so all notes can be recalculated afterward.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteWordsDialog = false
                        scope.launch {
                            viewModel.deleteAllBorromeanData()
                                .onSuccess {
                                    selectedKnotId = null
                                    previousKnotIds.clear()
                                    error = ""
                                }
                                .onFailure { error = it.message ?: "Could not delete Words data." }
                        }
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWordsDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
    if (showClearCalculationLogDialog) {
        AlertDialog(
            onDismissRequest = { showClearCalculationLogDialog = false },
            title = { Text("Delete calculation log?") },
            text = {
                Text(
                    "This clears the saved Gemma calculation log and progress status. It does not delete notes or extracted words.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCalculationLogDialog = false
                        viewModel.clearBorromeanCalculationLog()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCalculationLogDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun GemmaCalculationProgressBar(progress: BorromeanWordCalculationProgress) {
    val percent = (progress.fraction * 100f).toInt().coerceIn(0, 100)
    val remainingText = progress.estimatedRemainingMillis?.let { millis ->
        if (progress.isActive) "About ${formatShortDuration(millis)} left" else "Done"
    } ?: if (progress.isActive) {
        "Estimating time..."
    } else {
        "Done"
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gemma progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("$percent%", style = MaterialTheme.typography.labelMedium)
            }
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                progress.currentStep.ifBlank { "Preparing..." },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Elapsed ${formatShortDuration(progress.elapsedMillis)} - $remainingText",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun GemmaCalculationLog(logLines: List<String>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Calculation log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            logLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    onNewNote: (String) -> Unit,
    onEditNote: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var createError by rememberSaveable { mutableStateOf("") }
    val folders by viewModel.observeNoteFolders().collectAsState(initial = emptyList())
    val selectedFolder = folders.firstOrNull { it.folder.id == selectedFolderId }?.folder
    val searchScopeFolderId = selectedFolderId.takeIf { query.isBlank() || selectedFolder != null }
    val notesFlow = remember(searchScopeFolderId) {
        viewModel.observeNotesInFolder(searchScopeFolderId, "", "")
    }
    val notes by notesFlow.collectAsState(initial = emptyList())
    val searchResults = remember(notes, query) { rankedNoteSearchResults(notes, query) }
    val topHits = remember(searchResults, query) { if (query.isBlank()) emptyList() else searchResults.take(3) }
    var notePendingDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    var folderPendingDelete by remember { mutableStateOf<NoteFolderWithCount?>(null) }
    fun createNoteInCurrentFolder() {
        scope.launch {
            viewModel.startNote(selectedFolderId)
                .onSuccess {
                    createError = ""
                    onNewNote(it)
                }
                .onFailure { createError = it.message ?: "Could not create note." }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (selectedFolder != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { selectedFolderId = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to folders")
                        }
                        Text(selectedFolder.name, style = MaterialTheme.typography.headlineMedium)
                    }
                } else {
                    Text("Folders", style = MaterialTheme.typography.headlineMedium)
                }
            }
            item {
                ErrorText(createError)
            }
            if (query.isNotBlank()) {
                item {
                    Text("${searchResults.size} ${if (searchResults.size == 1) "note" else "notes"} found", style = MaterialTheme.typography.titleMedium)
                }
                if (topHits.isNotEmpty()) {
                    item { Text("Top Hits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(topHits, key = { "hit-${it.note.id}" }) { result ->
                        SwipeToDeleteRow(contentDescription = "Delete note", onDelete = { notePendingDelete = result.note }) {
                            NoteSearchResultRow(
                                result = result,
                                onClick = { onEditNote(result.note.id) },
                            )
                        }
                    }
                }
                item { Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(searchResults, key = { it.note.id }) { result ->
                    SwipeToDeleteRow(contentDescription = "Delete note", onDelete = { notePendingDelete = result.note }) {
                        NoteSearchResultRow(
                            result = result,
                            onClick = { onEditNote(result.note.id) },
                        )
                    }
                }
            } else if (selectedFolder == null) {
                items(folders, key = { it.folder.id }) { folder ->
                    if (folder.folder.isDefault) {
                        NoteFolderRow(folder = folder, onClick = { selectedFolderId = folder.folder.id })
                    } else {
                        SwipeToDeleteRow(
                            contentDescription = "Delete folder",
                            onDelete = { folderPendingDelete = folder },
                        ) {
                            NoteFolderRow(folder = folder, onClick = { selectedFolderId = folder.folder.id })
                        }
                    }
                }
            } else {
                item {
                    Text("${notes.size} ${if (notes.size == 1) "note" else "notes"}", style = MaterialTheme.typography.titleMedium)
                }
                items(notes, key = { it.id }) { note ->
                    val noteImages by viewModel.observeNoteImages(note.id).collectAsState(initial = emptyList())
                    val noteMemos by viewModel.observeMemos(note.id).collectAsState(initial = emptyList())
                    SwipeToDeleteRow(contentDescription = "Delete note", onDelete = { notePendingDelete = note }) {
                        NoteRow(
                            note = note,
                            images = noteImages,
                            memoCount = noteMemos.count { it.audioPath != null },
                            onClick = { onEditNote(note.id) },
                        )
                    }
                }
            }
        }
        NotesBottomSearchBar(
            query = query,
            onQueryChange = { query = it },
            onVoiceNote = ::createNoteInCurrentFolder,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 12.dp,
                ),
        )
    }

    notePendingDelete?.let { note ->
        DeleteNoteConfirmationDialog(
            note = note,
            onDismiss = { notePendingDelete = null },
            onDelete = {
                scope.launch {
                    viewModel.deleteConversation(note.id)
                        .onSuccess { notePendingDelete = null }
                        .onFailure { createError = it.message ?: "Could not delete note." }
                }
            },
        )
    }
    folderPendingDelete?.let { folder ->
        DeleteFolderConfirmationDialog(
            folder = folder,
            onDismiss = { folderPendingDelete = null },
            onDelete = {
                scope.launch {
                    viewModel.deleteNoteFolder(folder.folder.id)
                        .onSuccess {
                            if (selectedFolderId == folder.folder.id) {
                                selectedFolderId = null
                            }
                            folderPendingDelete = null
                        }
                        .onFailure { createError = it.message ?: "Could not delete folder." }
                }
            },
        )
    }

}

@Composable
private fun BorromeanObjectAHero(
    viewModel: MainViewModel,
    knot: BorromeanKnotWithWords,
    selectedRegister: String,
    rotationOffset: Float,
    stretchFactor: Float,
    onRegisterSelected: (String) -> Unit,
    onKnotTransform: (Float, Float) -> Unit,
    onEditWord: (BorromeanWordEntity) -> Unit,
    onAddObjectWord: () -> Unit,
    onDeleteObjectWord: (BorromeanWordEntity) -> Unit,
    onConnectWordNote: (BorromeanWordEntity) -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val objectWords = knot.words
        .filter { it.registerType == BORROMEAN_REGISTER_OBJECT_A }
        .sortedBy { it.sortOrder }
    val objectGroups = objectWords.groupBy { it.objectPartType?.takeIf { type -> type.isNotBlank() } ?: "other" }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF171326),
        contentColor = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            BorromeanKnotCanvas(
                words = objectWords,
                selectedRegister = selectedRegister,
                rotationOffset = rotationOffset,
                stretchFactor = stretchFactor,
                onKnotTransform = onKnotTransform,
                modifier = Modifier.fillMaxWidth().height(300.dp),
            )
            Spacer(Modifier.height(12.dp))
            ObjectACentralHoleStatement()
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                LacanRegisterChip(
                    label = "Real",
                    registerType = BORROMEAN_REGISTER_REAL,
                    selectedRegister = selectedRegister,
                    color = borromeanRegisterColor(BORROMEAN_REGISTER_REAL),
                    onClick = onRegisterSelected,
                    modifier = Modifier.weight(1f),
                )
                LacanRegisterChip(
                    label = "Symbolic",
                    registerType = BORROMEAN_REGISTER_SYMBOLIC,
                    selectedRegister = selectedRegister,
                    color = borromeanRegisterColor(BORROMEAN_REGISTER_SYMBOLIC),
                    onClick = onRegisterSelected,
                    modifier = Modifier.weight(1f),
                )
                LacanRegisterChip(
                    label = "Imaginary",
                    registerType = BORROMEAN_REGISTER_IMAGINARY,
                    selectedRegister = selectedRegister,
                    color = borromeanRegisterColor(BORROMEAN_REGISTER_IMAGINARY),
                    onClick = onRegisterSelected,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                borromeanRegisterDescription(selectedRegister),
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("object a fragments", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddObjectWord) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
            if (objectWords.isEmpty()) {
                Text("Add the leftover fragments the signifying chain cannot fully say.", color = Color.White.copy(alpha = 0.62f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    objectGroups.forEach { (type, words) ->
                        if (type != "other") {
                            Text(objectPartLabel(type), color = Color.White.copy(alpha = 0.58f), style = MaterialTheme.typography.labelSmall)
                        }
                        words.forEach { word ->
                            val colors = borromeanWordColors(word)
                            SwipeToDeleteRow(
                                contentDescription = "Delete word",
                                shape = RoundedCornerShape(16.dp),
                                onDelete = { onDeleteObjectWord(word) },
                            ) {
                                Surface(
                                    onClick = { onEditWord(word) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.container,
                                    contentColor = colors.content,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(word.text, fontWeight = FontWeight.Medium)
                                        GemmaWordMetadata(word = word, contentColor = colors.content)
                                        BorromeanWordNoteChip(
                                            viewModel = viewModel,
                                            word = word,
                                            onConnectNote = { onConnectWordNote(word) },
                                            onOpenNote = onOpenNote,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectACentralHoleStatement() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.10f),
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("object a is the central hole", fontWeight = FontWeight.SemiBold)
            Text(
                "The knot is organized around this hole. You can fill it with imaginary words, but it will never be that object, because object a is the missing place itself.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BorromeanKnotCanvas(
    words: List<BorromeanWordEntity>,
    selectedRegister: String,
    rotationOffset: Float,
    stretchFactor: Float,
    onKnotTransform: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "borromean")
    val rotationDrift by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(animation = tween(3600), repeatMode = RepeatMode.Reverse),
        label = "rotationDrift",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(animation = tween(1800), repeatMode = RepeatMode.Reverse),
        label = "pulse",
    )
    val stretch by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(animation = tween(4200), repeatMode = RepeatMode.Reverse),
        label = "stableStretch",
    )
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, rotation ->
                val rotationDelta = rotation + pan.x * 0.05f
                val stretchDelta = (zoom + pan.y * 0.001f).coerceIn(0.92f, 1.08f)
                onKnotTransform(rotationDelta, stretchDelta)
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringWidth = size.minDimension * 0.12f
            val projectedStretch = stretch * stretchFactor
            val ringSize = Size(size.minDimension * 0.72f * projectedStretch, size.minDimension * 0.43f / projectedStretch)
            val glowSize = Size(ringSize.width + ringWidth * 0.9f, ringSize.height + ringWidth * 0.9f)
            val ringTopLeft = Offset(center.x - ringSize.width / 2f, center.y - ringSize.height / 2f)
            val glowTopLeft = Offset(center.x - glowSize.width / 2f, center.y - glowSize.height / 2f)
            val configs = listOf(
                KnotRingConfig(BORROMEAN_REGISTER_REAL, -28f + rotationDrift.toFloat() + rotationOffset, borromeanRegisterColor(BORROMEAN_REGISTER_REAL)),
                KnotRingConfig(BORROMEAN_REGISTER_SYMBOLIC, 28f - rotationDrift.toFloat() + rotationOffset * 0.72f, borromeanRegisterColor(BORROMEAN_REGISTER_SYMBOLIC)),
                KnotRingConfig(BORROMEAN_REGISTER_IMAGINARY, 90f + rotationDrift * 0.7f - rotationOffset * 0.45f, borromeanRegisterColor(BORROMEAN_REGISTER_IMAGINARY)),
            )

            drawCircle(
                brush = ComposeBrush.radialGradient(
                    colors = listOf(Color(0x664B2B80), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.48f,
                ),
                radius = size.minDimension * 0.48f,
                center = center,
            )

            configs.forEach { config ->
                val selected = config.registerType == selectedRegister
                val selectedScale = if (selected) pulse else 0.78f
                val selectedWidth = if (selected) ringWidth * 1.18f else ringWidth * 0.82f
                rotate(config.rotation, pivot = center) {
                    drawOval(
                        color = config.color.copy(alpha = if (selected) 0.34f else 0.13f),
                        topLeft = glowTopLeft,
                        size = glowSize,
                        style = Stroke(width = ringWidth * 1.45f * selectedScale, cap = StrokeCap.Round),
                    )
                    drawOval(
                        color = Color.Black.copy(alpha = 0.22f),
                        topLeft = ringTopLeft + Offset(ringWidth * 0.12f, ringWidth * 0.20f),
                        size = ringSize,
                        style = Stroke(width = selectedWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = config.color.copy(alpha = if (selected) 0.98f else 0.60f),
                        startAngle = 12f,
                        sweepAngle = 118f,
                        useCenter = false,
                        topLeft = ringTopLeft,
                        size = ringSize,
                        style = Stroke(width = selectedWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = config.color.copy(alpha = if (selected) 0.58f else 0.34f),
                        startAngle = 146f,
                        sweepAngle = 88f,
                        useCenter = false,
                        topLeft = ringTopLeft,
                        size = ringSize,
                        style = Stroke(width = selectedWidth * 0.84f, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = config.color.copy(alpha = if (selected) 0.98f else 0.62f),
                        startAngle = 252f,
                        sweepAngle = 96f,
                        useCenter = false,
                        topLeft = ringTopLeft,
                        size = ringSize,
                        style = Stroke(width = selectedWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.36f),
                        startAngle = 34f,
                        sweepAngle = 72f,
                        useCenter = false,
                        topLeft = ringTopLeft + Offset(0f, -ringWidth * 0.18f),
                        size = ringSize,
                        style = Stroke(width = selectedWidth * 0.22f, cap = StrokeCap.Round),
                    )
                }
            }

            drawCircle(Color(0xFF171326).copy(alpha = 0.92f), radius = size.minDimension * 0.18f, center = center)
            drawCircle(Color.White.copy(alpha = 0.12f), radius = size.minDimension * 0.19f, center = center, style = Stroke(width = 2.5f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("object a", color = Color.White.copy(alpha = 0.70f), style = MaterialTheme.typography.labelMedium)
            Text("central hole", color = Color.White.copy(alpha = 0.58f), style = MaterialTheme.typography.labelSmall)
            words.take(4).forEach { word ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.20f),
                    contentColor = Color.White,
                ) {
                    Text(
                        word.text,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Text(
            "stable while stretched",
            color = Color.White.copy(alpha = 0.42f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun LacanRegisterChip(
    label: String,
    registerType: String,
    selectedRegister: String,
    color: Color,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = registerType == selectedRegister
    Surface(
        modifier = modifier,
        onClick = { onClick(registerType) },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (selected) 0.34f else 0.16f),
        contentColor = Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, color.copy(alpha = if (selected) 0.90f else 0.50f)),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WeightedBorromeanWordSection(
    viewModel: MainViewModel,
    title: String,
    description: String,
    words: List<BorromeanWordEntity>,
    primaryLabel: String,
    secondaryLabel: String,
    primaryValue: (BorromeanWordEntity) -> Int,
    secondaryValue: (BorromeanWordEntity) -> Int,
    onAddWord: () -> Unit,
    onEditWord: (BorromeanWordEntity) -> Unit,
    onDeleteWord: (BorromeanWordEntity) -> Unit,
    onConnectNote: (BorromeanWordEntity) -> Unit,
    onOpenNote: (String) -> Unit,
    onPrimaryChange: (BorromeanWordEntity, Int) -> Unit,
    onSecondaryChange: (BorromeanWordEntity, Int) -> Unit,
) {
    Surface(shape = RoundedCornerShape(22.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddWord) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (words.isEmpty()) {
                Text("No words yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                words.forEach { word ->
                    SwipeToDeleteRow(
                        contentDescription = "Delete word",
                        shape = RoundedCornerShape(18.dp),
                        onDelete = { onDeleteWord(word) },
                    ) {
                        WeightedBorromeanWordRow(
                            viewModel = viewModel,
                            word = word,
                            primaryLabel = primaryLabel,
                            secondaryLabel = secondaryLabel,
                            primaryValue = primaryValue(word),
                            secondaryValue = secondaryValue(word),
                            onEdit = { onEditWord(word) },
                            onConnectNote = { onConnectNote(word) },
                            onOpenNote = onOpenNote,
                            onPrimaryChange = { onPrimaryChange(word, it) },
                            onSecondaryChange = { onSecondaryChange(word, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightedBorromeanWordRow(
    viewModel: MainViewModel,
    word: BorromeanWordEntity,
    primaryLabel: String,
    secondaryLabel: String,
    primaryValue: Int,
    secondaryValue: Int,
    onEdit: () -> Unit,
    onConnectNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onPrimaryChange: (Int) -> Unit,
    onSecondaryChange: (Int) -> Unit,
) {
    val colors = borromeanWordColors(word)
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.42f)),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(word.text, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit word")
                }
            }
            GemmaWordMetadata(word = word, contentColor = colors.content)
            BorromeanWordNoteChip(
                viewModel = viewModel,
                word = word,
                onConnectNote = onConnectNote,
                onOpenNote = onOpenNote,
                modifier = Modifier.fillMaxWidth(),
            )
            WeightStepper(label = primaryLabel, value = primaryValue, onChange = onPrimaryChange)
            WeightStepper(label = secondaryLabel, value = secondaryValue, onChange = onSecondaryChange)
        }
    }
}

@Composable
private fun GemmaWordMetadata(word: BorromeanWordEntity, contentColor: Color) {
    if (word.source != "gemma") return
    AssistChip(onClick = {}, label = { Text("Extracted by Gemma") })
    if (word.extractionEvidence?.isNotBlank() == true) {
        Text(
            word.extractionEvidence,
            color = contentColor.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WeightStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label $value", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(
            onClick = { onChange((value - 5).coerceIn(0, 100)) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)),
        ) {
            Text("-")
        }
        OutlinedButton(
            onClick = { onChange((value + 5).coerceIn(0, 100)) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)),
        ) {
            Text("+")
        }
    }
}

@Composable
private fun BorromeanWordNoteChip(
    viewModel: MainViewModel,
    word: BorromeanWordEntity,
    onConnectNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val conversationId = word.conversationId
    if (conversationId == null) {
        val colors = borromeanWordColors(word)
        Surface(
            onClick = onConnectNote,
            modifier = modifier,
            shape = RoundedCornerShape(50),
            color = colors.chipContainer,
            contentColor = colors.chipContent,
            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.46f)),
        ) {
            Text(
                "Connect note",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontWeight = FontWeight.Medium,
            )
        }
    } else {
        val colors = borromeanWordColors(word)
        val note by viewModel.observeConversation(conversationId).collectAsState(initial = null)
        val label = remember(note) {
            note?.let { linkedNote ->
                linkedNote.title.takeIf { it.isNotBlank() }
                    ?: plainNoteText(linkedNote.finalNote).lineSequence().firstOrNull { it.isNotBlank() }
                    ?: "Linked note"
            } ?: "Linked note"
        }
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(50),
            color = colors.chipContainer,
            contentColor = colors.chipContent,
            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.46f)),
        ) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                TextButton(onClick = { onOpenNote(conversationId) }) {
                    Text("Open")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BorromeanNoteConnectionSheet(
    viewModel: MainViewModel,
    wordText: String,
    linkedConversationId: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConnectExisting: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNew: () -> Unit,
) {
    val notesFlow = remember(query) { viewModel.observeNotes(null, query, "", "") }
    val notes by notesFlow.collectAsState(initial = emptyList())
    val matches = remember(notes, query, linkedConversationId) {
        rankedNoteSearchResults(notes, query)
            .filter { it.note.id != linkedConversationId }
            .take(6)
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Connect note", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Find notes related to \"$wordText\", connect one, or create a new linked note.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Search notes") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Top matches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (matches.isEmpty()) {
                Text("No matching notes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                matches.forEach { result ->
                    NoteConnectionResultRow(
                        result = result,
                        onConnect = { onConnectExisting(result.note.id) },
                        onOpen = { onOpenNote(result.note.id) },
                    )
                }
            }
            HorizontalDivider()
            Button(onClick = onCreateNew, modifier = Modifier.fillMaxWidth()) {
                Text("Create new linked note")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NoteConnectionResultRow(
    result: NoteSearchResult,
    onConnect: () -> Unit,
    onOpen: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(result.note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold)
            Text(
                result.preview.ifBlank { "No preview available." },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f)) {
                    Text("Open")
                }
                Button(onClick = onConnect, modifier = Modifier.weight(1f)) {
                    Text("Connect")
                }
            }
        }
    }
}

@Composable
private fun ObjectAKnotCard(
    knot: BorromeanKnotWithWords,
    onClick: () -> Unit,
) {
    val objectWords = knot.words
        .filter { it.registerType == BORROMEAN_REGISTER_OBJECT_A }
        .map { it.text }
        .ifEmpty { knot.words.take(3).map { it.text } }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniBorromeanKnot(modifier = Modifier.size(82.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(knot.knot.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(knot.knot.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(objectWords.joinToString(" / "), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MiniBorromeanKnot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val ringSize = Size(size.width * 0.68f, size.height * 0.38f)
        val topLeft = Offset(center.x - ringSize.width / 2f, center.y - ringSize.height / 2f)
        listOf(
            KnotRingConfig(BORROMEAN_REGISTER_REAL, -28f, borromeanRegisterColor(BORROMEAN_REGISTER_REAL)),
            KnotRingConfig(BORROMEAN_REGISTER_SYMBOLIC, 28f, borromeanRegisterColor(BORROMEAN_REGISTER_SYMBOLIC)),
            KnotRingConfig(BORROMEAN_REGISTER_IMAGINARY, 90f, borromeanRegisterColor(BORROMEAN_REGISTER_IMAGINARY)),
        ).forEach { config ->
            rotate(config.rotation, pivot = center) {
                drawOval(config.color, topLeft = topLeft, size = ringSize, style = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
private fun BorromeanWordDialog(
    title: String,
    registerType: String,
    text: String,
    objectPartType: String,
    emotionalWeight: Int,
    importanceWeight: Int,
    desireWeight: Int,
    onTextChange: (String) -> Unit,
    onObjectPartTypeChange: (String) -> Unit,
    onEmotionalWeightChange: (Int) -> Unit,
    onImportanceWeightChange: (Int) -> Unit,
    onDesireWeightChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Register: ${borromeanRegisterLabel(registerType)}")
                Text(
                    borromeanRegisterDescription(registerType),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    label = { Text("Word or phrase") },
                )
                if (registerType == BORROMEAN_REGISTER_OBJECT_A) {
                    OutlinedTextField(
                        value = objectPartType,
                        onValueChange = onObjectPartTypeChange,
                        singleLine = true,
                        label = { Text("Object part type") },
                        placeholder = { Text("gaze, voice, breast, excrement") },
                    )
                }
                if (registerType == BORROMEAN_REGISTER_AFFECT) {
                    WeightStepper("Personal importance", importanceWeight, onImportanceWeightChange)
                    WeightStepper("Emotional attachment", emotionalWeight, onEmotionalWeightChange)
                }
                if (registerType == BORROMEAN_REGISTER_DESIRE) {
                    WeightStepper("Desire strength", desireWeight, onDesireWeightChange)
                    WeightStepper("Importance", importanceWeight, onImportanceWeightChange)
                }
                Text(
                    "Saving will create or open a note where you can describe the memory, association, or remainder linked to this signifier.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun NoteFolderRow(
    folder: NoteFolderWithCount,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(folder.folder.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "${folder.noteCount} ${if (folder.noteCount == 1) "note" else "notes"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

@Composable
private fun NoteSearchResultRow(
    result: NoteSearchResult,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (result.note.isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        result.note.title.ifBlank { "Untitled note" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text("${result.matchCount}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(formatDate(result.note.createdAt), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            if (result.note.isLocked) {
                Spacer(Modifier.height(8.dp))
                Text("Locked Note", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (result.preview.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(result.preview, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun NotesBottomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onVoiceNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 5.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                placeholder = { Text("Search") },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onVoiceNote) {
                Icon(Icons.Default.Mic, contentDescription = "New voice note")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(
    contentDescription: String,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error, shape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onError)
            }
        },
        content = { content() },
    )
}

@Composable
fun PrivacyScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
) {
    val usage by viewModel.storageUsage.collectAsState()
    val modelSettings by viewModel.modelSettings.collectAsState()
    val gemmaDownloadProgress by viewModel.gemmaDownloadProgress.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showWordsExportDialog by rememberSaveable { mutableStateOf(false) }
    var showAudioExportDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAudioDialog by rememberSaveable { mutableStateOf(false) }
    var deleteAudioConfirmation by rememberSaveable { mutableStateOf("") }
    var modelPath by rememberSaveable { mutableStateOf("") }
    var modelStatus by rememberSaveable { mutableStateOf("") }
    var modelSetupMessage by rememberSaveable { mutableStateOf("") }
    val isGemmaDownloadActive = gemmaDownloadProgress.isActive
    val isGemmaModelReady = modelSettings.isExpectedGemmaModel
    val isGemmaSetupLocked = isGemmaDownloadActive || isGemmaModelReady
    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                modelSetupMessage = "Importing Gemma 4 E2B model into app storage..."
                viewModel.importGemmaModel(uri)
                    .onSuccess { path ->
                        modelPath = path
                        modelStatus = viewModel.gemmaModelStatus().getOrDefault("")
                        modelSetupMessage = "Gemma 4 E2B model imported."
                    }
                    .onFailure { error ->
                        modelSetupMessage = error.message ?: "Could not import Gemma 4 E2B model."
                    }
            }
        }
    }
    val progress = (usage.usedBytes.toFloat() / usage.limitBytes.toFloat()).coerceIn(0f, 1f)
    LaunchedEffect(modelSettings.gemmaModelPath) {
        modelPath = modelSettings.gemmaModelPath
        modelStatus = viewModel.gemmaModelStatus().getOrDefault("")
    }
    LaunchedEffect(gemmaDownloadProgress.status) {
        when (gemmaDownloadProgress.status) {
            GemmaModelDownloadProgress.Status.Successful -> {
                modelStatus = viewModel.gemmaModelStatus().getOrDefault("")
                modelSetupMessage = "Gemma 4 E2B is downloaded and imported automatically. The app will use it for extraction."
            }
            GemmaModelDownloadProgress.Status.Failed -> {
                modelSetupMessage = gemmaDownloadProgress.reason.ifBlank {
                    "Gemma 4 E2B download failed. Open the download page, accept the license if needed, then import the .litertlm file."
                }
            }
            else -> Unit
        }
    }

    Page(contentPadding = contentPadding) {
        item {
            Text("Privacy and storage", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Text("Local app data", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("${formatBytes(usage.usedBytes)} / ${formatBytes(usage.limitBytes)}")
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(18.dp))
            Text(
                "Notes are precious, so they can only be deleted one at a time from the Notes page. When app data is larger than 1 GB, the oldest audio is deleted. Notes are kept. Saved words are managed in My Words.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            OutlinedButton(
                onClick = {
                    deleteAudioConfirmation = ""
                    showDeleteAudioDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete saved voice memo audio")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export notes only")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showWordsExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export words")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showAudioExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export audios")
            }
            Spacer(Modifier.height(22.dp))
            Text("Gemma on-device model", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Use Gemma 4 E2B on this device to extract object a words, personally important words, and what I truly want from notes and voice memos. E2B is smaller than E4B and is a better fit for Pixel 6a. Tap Download to this app once. When the download finishes, the model is imported automatically into private app storage.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Official model file: $GEMMA_4_E2B_MODEL_NAME",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openUrl(context, GEMMA_4_E2B_MODEL_PAGE_URL) },
                enabled = !isGemmaSetupLocked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Gemma 4 E2B download page")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        viewModel.startGemmaModelDownload()
                            .onSuccess {
                                modelStatus = viewModel.gemmaModelStatus().getOrDefault("")
                                modelSetupMessage = "Downloading Gemma 4 E2B. Keep this screen open to see progress."
                            }
                            .onFailure { error ->
                                modelSetupMessage = error.message ?: "Could not start Gemma 4 E2B download."
                            }
                    }
                },
                enabled = !isGemmaSetupLocked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        isGemmaDownloadActive -> "Downloading Gemma 4 E2B..."
                        isGemmaModelReady -> "Gemma 4 E2B ready"
                        else -> "Download to this app"
                    },
                )
            }
            if (gemmaDownloadProgress.status != GemmaModelDownloadProgress.Status.Idle) {
                Spacer(Modifier.height(8.dp))
                GemmaDownloadProgressView(progress = gemmaDownloadProgress)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { modelPicker.launch(arrayOf("application/octet-stream", "*/*")) },
                enabled = !isGemmaSetupLocked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import downloaded .litertlm")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = modelPath,
                onValueChange = { if (!isGemmaSetupLocked) modelPath = it },
                enabled = !isGemmaSetupLocked,
                label = { Text("Local .litertlm path (advanced)") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        viewModel.setGemmaModelPath(modelPath)
                        modelStatus = viewModel.gemmaModelStatus().getOrDefault("")
                    }
                },
                enabled = !isGemmaSetupLocked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Gemma model path")
            }
            if (modelSetupMessage.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(modelSetupMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (modelStatus.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(modelStatus, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            title = "Export notes only?",
            includedItems = listOf("your written notes", "dates", "linked confirmed nouns"),
            excludedItems = listOf("audio", "note images"),
            onDismiss = { showExportDialog = false },
            onExport = {
                showExportDialog = false
                scope.launch {
                    viewModel.createNotesOnlyExport()
                        .onSuccess { shareTextFile(context, it, "Export notes only") }
                }
            },
        )
    }
    if (showWordsExportDialog) {
        ExportDialog(
            title = "Export words?",
            includedItems = listOf("saved words", "object a fragments", "personally important words", "what I truly want"),
            excludedItems = listOf("audio", "note images"),
            onDismiss = { showWordsExportDialog = false },
            onExport = {
                showWordsExportDialog = false
                scope.launch {
                    viewModel.createWordsExport()
                        .onSuccess { shareTextFile(context, it, "Export words") }
                }
            },
        )
    }
    if (showAudioExportDialog) {
        ExportDialog(
            title = "Export audios?",
            includedItems = listOf("saved voice memo audio files"),
            excludedItems = listOf("written note text", "words", "note images", "deleted audio"),
            onDismiss = { showAudioExportDialog = false },
            onExport = {
                showAudioExportDialog = false
                scope.launch {
                    viewModel.createAllAudioExport()
                        .onSuccess { shareAudioExport(context, it, "Export audios") }
                }
            },
        )
    }
    if (showDeleteAudioDialog) {
        DeleteAudioConfirmationDialog(
            confirmationText = deleteAudioConfirmation,
            onConfirmationTextChange = { deleteAudioConfirmation = it },
            onDismiss = { showDeleteAudioDialog = false },
            onDelete = {
                showDeleteAudioDialog = false
                deleteAudioConfirmation = ""
                scope.launch { viewModel.deleteOldAudioNow() }
            },
        )
    }
}

@Composable
private fun GemmaDownloadProgressView(progress: GemmaModelDownloadProgress) {
    val percent = progress.percent
    val title = when (progress.status) {
        GemmaModelDownloadProgress.Status.Pending -> "Waiting to download Gemma 4 E2B"
        GemmaModelDownloadProgress.Status.Running -> if (percent != null) {
            "Downloading Gemma 4 E2B: $percent%"
        } else {
            "Downloading Gemma 4 E2B"
        }
        GemmaModelDownloadProgress.Status.Successful -> "Gemma 4 E2B ready"
        GemmaModelDownloadProgress.Status.Failed -> "Gemma 4 E2B download failed"
        GemmaModelDownloadProgress.Status.Idle -> "Gemma 4 E2B download"
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (percent != null) {
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "${formatBytes(progress.downloadedBytes)} of ${formatBytes(progress.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if (progress.isActive) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Preparing the download size...", style = MaterialTheme.typography.bodySmall)
            }
            if (progress.reason.isNotBlank()) {
                Text(progress.reason, style = MaterialTheme.typography.bodySmall)
            } else if (progress.isActive) {
                Text(
                    "Model setup is locked during download. When it finishes, the model is imported automatically.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun Page(
    contentPadding: PaddingValues,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

private enum class NoteFormat {
    Title,
    Heading,
    Subheading,
    Body,
    Bold,
    Italic,
    Underline,
    Strikethrough,
    BulletList,
    NumberedList,
    Quote,
}

private val NoteFormat.isInline: Boolean
    get() = this in setOf(NoteFormat.Bold, NoteFormat.Italic, NoteFormat.Underline, NoteFormat.Strikethrough)

private fun TextFieldValue.insertAtSelection(insert: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val cleanInsert = if (text.isBlank()) insert.trimStart('\n') else insert
    val newText = text.replaceRange(start, end, cleanInsert)
    val newCursor = start + cleanInsert.length
    return copy(text = newText, selection = TextRange(newCursor))
}

private val attachmentTokenRegex = Regex("\\[\\[(image|drawing|audio|file):([^\\]]+)]]")
private val tableTokenRegex = Regex("\\[\\[table:([^\\]]+)]]")

private fun TextFieldValue.applyNoteFormat(format: NoteFormat): TextFieldValue =
    when (format) {
        NoteFormat.Title -> applyParagraphPrefix("# ")
        NoteFormat.Heading -> applyParagraphPrefix("## ")
        NoteFormat.Subheading -> applyParagraphPrefix("### ")
        NoteFormat.Body -> applyParagraphPrefix("")
        NoteFormat.Bold -> wrapSelection("**", "**", "bold")
        NoteFormat.Italic -> wrapSelection("*", "*", "italic")
        NoteFormat.Underline -> wrapSelection("<u>", "</u>", "underlined")
        NoteFormat.Strikethrough -> wrapSelection("~~", "~~", "struck text")
        NoteFormat.BulletList -> applyParagraphPrefix("- ")
        NoteFormat.NumberedList -> applyParagraphPrefix("1. ")
        NoteFormat.Quote -> applyParagraphPrefix("> ")
    }

private fun TextFieldValue.applyParagraphPrefix(prefix: String): TextFieldValue {
    val textStart = selection.min
    val textEnd = selection.max
    val lineStart = text.lastIndexOf('\n', (textStart - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', textEnd).let { if (it == -1) text.length else it }
    if (lineStart >= lineEnd && prefix.isNotEmpty()) {
        return insertAtSelection(prefix)
    }
    val line = text.substring(lineStart, lineEnd)
    val oldPrefixLength = paragraphPrefixLength(line)
    val replacement = prefix + line.drop(oldPrefixLength)
    val newText = text.replaceRange(lineStart, lineEnd, replacement)
    val shift = prefix.length - oldPrefixLength
    return copy(
        text = newText,
        selection = TextRange(
            (selection.start + shift).coerceIn(0, newText.length),
            (selection.end + shift).coerceIn(0, newText.length),
        ),
    )
}

private fun paragraphPrefixLength(line: String): Int =
    when {
        line.startsWith("### ") -> 4
        line.startsWith("## ") -> 3
        line.startsWith("# ") -> 2
        line.startsWith("> ") -> 2
        line.startsWith("- ") -> 2
        else -> Regex("^\\d+\\.\\s").find(line)?.value?.length ?: 0
    }

private fun TextFieldValue.wrapSelection(prefix: String, suffix: String, placeholder: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val selected = text.substring(start, end).ifEmpty { placeholder }
    val replacement = prefix + selected + suffix
    val newText = text.replaceRange(start, end, replacement)
    val selectedStart = start + prefix.length
    return copy(
        text = newText,
        selection = TextRange(selectedStart, selectedStart + selected.length),
    )
}

private class RichNoteVisualTransformation(
    private val findQuery: String = "",
    private val selectedFindRange: IntRange? = null,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val renderer = RichNoteRenderer(text.text, findQuery, selectedFindRange)
        return TransformedText(renderer.rendered, renderer.offsetMapping)
    }
}

private class RichNoteRenderer(
    private val source: String,
    findQuery: String = "",
    private val selectedFindRange: IntRange? = null,
) {
    private val builder = AnnotatedString.Builder()
    private val originalToTransformed = IntArray(source.length + 1)
    private val transformedCharOriginals = mutableListOf<Int>()
    private val findRanges = findVisibleMatchRanges(source, findQuery)

    val rendered: AnnotatedString
    val offsetMapping: OffsetMapping

    init {
        render()
        originalToTransformed[source.length] = transformedCharOriginals.size
        val transformedToOriginal = IntArray(transformedCharOriginals.size + 1)
        transformedToOriginal[0] = 0
        transformedCharOriginals.forEachIndexed { index, original ->
            transformedToOriginal[index + 1] = (original + 1).coerceAtMost(source.length)
        }
        rendered = builder.toAnnotatedString()
        offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, originalToTransformed.lastIndex)]

            override fun transformedToOriginal(offset: Int): Int =
                transformedToOriginal[offset.coerceIn(0, transformedToOriginal.lastIndex)]
        }
    }

    private fun render() {
        var lineStart = 0
        while (lineStart < source.length) {
            val lineEnd = source.indexOf('\n', lineStart).let { if (it == -1) source.length else it }
            renderLine(lineStart, lineEnd)
            if (lineEnd < source.length) {
                appendVisible(lineEnd, SpanStyle())
                lineStart = lineEnd + 1
            } else {
                lineStart = lineEnd
            }
        }
    }

    private fun renderLine(start: Int, end: Int) {
        val line = source.substring(start, end)
        val prefixLength = paragraphPrefixLength(line)
        val style = when {
            line.startsWith("# ") -> SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            line.startsWith("## ") -> SpanStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            line.startsWith("### ") -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
            line.startsWith("> ") -> SpanStyle(fontStyle = FontStyle.Italic)
            else -> SpanStyle()
        }
        hideRange(start, start + prefixLength)
        when {
            line.startsWith("- ") -> appendSynthetic(start, "\u2022 ", style)
            Regex("^\\d+\\.\\s").find(line) != null -> appendSynthetic(start, line.take(prefixLength), style)
            line.startsWith("> ") -> appendSynthetic(start, "| ", style)
        }
        renderInline(start + prefixLength, end, style)
    }

    private fun renderInline(start: Int, end: Int, baseStyle: SpanStyle) {
        var index = start
        while (index < end) {
            when {
                source.startsWith("[ ] ", index) -> {
                    hideRange(index, index + 4)
                    appendSynthetic(index, "\u2610 ", baseStyle)
                    index += 4
                }
                source.startsWith("[x] ", index, ignoreCase = true) -> {
                    hideRange(index, index + 4)
                    appendSynthetic(index, "\u2611 ", baseStyle)
                    index += 4
                }
                attachmentTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < end } != null -> {
                    val match = requireNotNull(attachmentTokenRegex.find(source, index))
                    hideRange(match.range.first, match.range.last + 1)
                    val label = when (match.groupValues[1]) {
                        "image" -> "[Image]"
                        "drawing" -> "[Drawing]"
                        "audio" -> "[Audio]"
                        else -> "[File]"
                    }
                    appendSynthetic(index, label, baseStyle.copy(fontWeight = FontWeight.Medium))
                    index = match.range.last + 1
                }
                tableTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < end } != null -> {
                    val match = requireNotNull(tableTokenRegex.find(source, index))
                    hideRange(match.range.first, match.range.last + 1)
                    appendSynthetic(index, renderTableToken(match.groupValues[1]), baseStyle)
                    index = match.range.last + 1
                }
                source.startsWith("**", index) -> {
                    val close = source.indexOf("**", index + 2).takeIf { it in (index + 2) until end }
                    if (close != null) {
                        hideRange(index, index + 2)
                        renderInline(index + 2, close, baseStyle.copy(fontWeight = FontWeight.Bold))
                        hideRange(close, close + 2)
                        index = close + 2
                    } else {
                        appendVisible(index, baseStyle)
                        index++
                    }
                }
                source.startsWith("~~", index) -> {
                    val close = source.indexOf("~~", index + 2).takeIf { it in (index + 2) until end }
                    if (close != null) {
                        hideRange(index, index + 2)
                        renderInline(index + 2, close, baseStyle.copy(textDecoration = TextDecoration.LineThrough))
                        hideRange(close, close + 2)
                        index = close + 2
                    } else {
                        appendVisible(index, baseStyle)
                        index++
                    }
                }
                source.startsWith("<u>", index) -> {
                    val close = source.indexOf("</u>", index + 3).takeIf { it in (index + 3) until end }
                    if (close != null) {
                        hideRange(index, index + 3)
                        renderInline(index + 3, close, baseStyle.copy(textDecoration = TextDecoration.Underline))
                        hideRange(close, close + 4)
                        index = close + 4
                    } else {
                        appendVisible(index, baseStyle)
                        index++
                    }
                }
                source[index] == '*' -> {
                    val close = source.indexOf('*', index + 1).takeIf { it in (index + 1) until end }
                    if (close != null) {
                        hideRange(index, index + 1)
                        renderInline(index + 1, close, baseStyle.copy(fontStyle = FontStyle.Italic))
                        hideRange(close, close + 1)
                        index = close + 1
                    } else {
                        appendVisible(index, baseStyle)
                        index++
                    }
                }
                else -> {
                    val next = nextInlineMarker(index, end).coerceAtLeast(index + 1)
                    appendVisibleRange(index, next, baseStyle)
                    index = next
                }
            }
        }
    }

    private fun nextInlineMarker(start: Int, end: Int): Int {
        val candidates = listOf("**", "~~", "<u>", "*", "[[", "[ ] ", "[x] ")
            .mapNotNull { marker -> source.indexOf(marker, start).takeIf { it in start until end } }
        return candidates.minOrNull() ?: end
    }

    private fun hideRange(start: Int, end: Int) {
        val transformed = builder.length
        for (index in start until end.coerceAtMost(source.length)) {
            originalToTransformed[index] = transformed
        }
    }

    private fun appendVisible(originalIndex: Int, style: SpanStyle) {
        originalToTransformed[originalIndex] = builder.length
        builder.withStyle(style.mergedWithFindStyle(originalIndex)) {
            append(source[originalIndex])
        }
        transformedCharOriginals.add(originalIndex)
        originalToTransformed[originalIndex + 1] = builder.length
    }

    private fun appendVisibleRange(start: Int, end: Int, style: SpanStyle) {
        if (start >= end) return
        for (index in start until end) {
            originalToTransformed[index] = builder.length + (index - start)
            transformedCharOriginals.add(index)
        }
        for (index in start until end) {
            builder.withStyle(style.mergedWithFindStyle(index)) {
                append(source[index])
            }
        }
        originalToTransformed[end] = builder.length
    }

    private fun appendSynthetic(anchorOriginal: Int, text: String, style: SpanStyle) {
        builder.withStyle(style) {
            append(text)
        }
        repeat(text.length) {
            transformedCharOriginals.add(anchorOriginal)
        }
    }

    private fun SpanStyle.mergedWithFindStyle(originalIndex: Int): SpanStyle {
        val inSelected = selectedFindRange?.let { originalIndex in it } == true
        val inMatch = inSelected || findRanges.any { originalIndex in it }
        return when {
            inSelected -> merge(SpanStyle(background = Color(0xFFFFC107), color = Color.Black))
            inMatch -> merge(SpanStyle(background = Color(0xFFFFF59D), color = Color.Black))
            else -> this
        }
    }
}

private fun renderTableToken(payload: String): String {
    return parseTablePayload(payload).joinToString("\n") { row -> row.joinToString(" | ") }
}

private data class InlineAttachment(
    val type: String,
    val id: String,
)

private sealed interface NoteBlock {
    val start: Int
    val end: Int

    data class TextSegment(override val start: Int, override val end: Int, val text: String) : NoteBlock
    data class Checklist(
        override val start: Int,
        override val end: Int,
        val checked: Boolean,
        val text: String,
    ) : NoteBlock
    data class Table(override val start: Int, override val end: Int, val payload: String) : NoteBlock
    data class Attachment(override val start: Int, override val end: Int, val type: String, val id: String) : NoteBlock
}

private val noteTokenRegex = Regex("\\[\\[(image|drawing|audio|file):([^\\]]+)]]|\\[\\[table:([^\\]]+)]]|\\[\\[check:([01]):([^\\]]*)]]")

private fun parseNoteBlocks(text: String): List<NoteBlock> {
    val blocks = mutableListOf<NoteBlock>()
    var cursor = 0
    noteTokenRegex.findAll(text).forEach { match ->
        appendTextBlocks(text, cursor, match.range.first, blocks)
        val type = match.groupValues[1]
        blocks += when {
            type.isNotEmpty() -> NoteBlock.Attachment(match.range.first, match.range.last + 1, type, match.groupValues[2])
            match.groupValues[3].isNotEmpty() -> NoteBlock.Table(match.range.first, match.range.last + 1, match.groupValues[3])
            else -> NoteBlock.Checklist(
                start = match.range.first,
                end = match.range.last + 1,
                checked = match.groupValues[4] == "1",
                text = match.groupValues[5],
            )
        }
        cursor = match.range.last + 1
    }
    appendTextBlocks(text, cursor, text.length, blocks)
    return blocks
}

private fun appendTextBlocks(text: String, start: Int, end: Int, blocks: MutableList<NoteBlock>) {
    if (start >= end) return
    blocks += NoteBlock.TextSegment(start, end, text.substring(start, end))
}

private val noteEditorMinHeight = 360.dp
private val noteEditorLineHeight = 24.sp

private fun Modifier.noteBlankTapTarget(
    value: TextFieldValue,
    contentHeightPx: Int,
    insertSingleRowOnly: Boolean,
    onEditorTap: () -> Unit,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
): Modifier {
    return fillMaxWidth()
        .heightIn(min = noteEditorMinHeight)
        .pointerInput(value.text, contentHeightPx, focusRequester) {
            detectTapGestures { offset ->
                onEditorTap()
                if (offset.y <= contentHeightPx) return@detectTapGestures
                val lineHeightPx = noteEditorLineHeight.toPx().coerceAtLeast(1f)
                val missingRows = if (insertSingleRowOnly) {
                    1
                } else {
                    (((offset.y - contentHeightPx) / lineHeightPx).toInt() + 1).coerceIn(1, 12)
                }
                val insertAt = value.text.length
                val insertedRows = "\n".repeat(missingRows)
                val nextText = value.text.replaceRange(insertAt, insertAt, insertedRows)
                val cursor = insertAt + insertedRows.length
                onValueChange(value.copy(text = nextText, selection = TextRange(cursor)))
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NoteDocumentEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    memos: List<VoiceMemoEntity>,
    images: List<NoteImageEntity>,
    activeInlineFormats: Set<NoteFormat>,
    onFocusChange: (Boolean) -> Unit,
    onEditorTap: () -> Unit,
    onDeleteAudio: (String) -> Unit,
    playbackState: AudioPlaybackUiState,
    onPlayPauseMemo: (VoiceMemoEntity) -> Unit,
    onSeekMemo: (Int) -> Unit,
    onDeleteImage: (String) -> Unit,
    findQuery: String = "",
    selectedFindRange: IntRange? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val blocks = remember(value.text) { parseNoteBlocks(value.text) }
    val memoById = remember(memos) { memos.associateBy { it.id } }
    val imageById = remember(images) { images.associateBy { it.id } }
    val tailFocusRequester = remember { FocusRequester() }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    val hasEditableTail = value.text.isEmpty() ||
        value.text.endsWith('\n') ||
        blocks.lastOrNull() is NoteBlock.TextSegment
    val blankTapCreatesTailLine = value.text.isNotEmpty() && blocks.lastOrNull() !is NoteBlock.TextSegment

    Box(
        modifier = modifier.noteBlankTapTarget(
            value = value,
            contentHeightPx = contentSize.height,
            insertSingleRowOnly = blankTapCreatesTailLine,
            onEditorTap = onEditorTap,
            onValueChange = onValueChange,
            focusRequester = tailFocusRequester,
            keyboardController = keyboardController,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { contentSize = it },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (value.text.isBlank()) {
                Text("Start writing", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
            val renderedBlocks = blocks.ifEmpty { listOf(NoteBlock.TextSegment(0, 0, "")) }
            renderedBlocks.forEachIndexed { index, block ->
                when (block) {
                    is NoteBlock.TextSegment -> NoteTextSegmentEditor(
                        block = block,
                        value = value,
                        onValueChange = onValueChange,
                        activeInlineFormats = activeInlineFormats,
                        findQuery = findQuery,
                        selectedFindRange = selectedFindRange,
                        onFocusChange = onFocusChange,
                        focusRequester = if (index == renderedBlocks.lastIndex) tailFocusRequester else null,
                    )
                    is NoteBlock.Checklist -> ChecklistBlockEditor(block, value, onValueChange, onFocusChange)
                    is NoteBlock.Table -> EditableTableBlock(block, value, onValueChange, onFocusChange)
                    is NoteBlock.Attachment -> when (block.type) {
                        "audio" -> memoById[block.id]?.let { memo ->
                            MemoRow(
                                index = memos.indexOfFirst { it.id == memo.id }.takeIf { it >= 0 }?.plus(1) ?: 1,
                                memo = memo,
                                playbackState = playbackState,
                                onPlayPause = { onPlayPauseMemo(memo) },
                                onSeek = onSeekMemo,
                                onDeleteAudio = { onDeleteAudio(memo.id) },
                            )
                        }
                        "image", "drawing" -> imageById[block.id]?.let { image ->
                            NoteImageRow(image = image, onDelete = { onDeleteImage(image.id) })
                        }
                        "file" -> {
                            val attachment = parseFileAttachmentToken(block.id)
                            FileAttachmentRow(
                                attachment = attachment,
                                onOpen = { openFileAttachment(context, attachment) },
                                onDelete = {
                                    deleteFileAttachment(attachment)
                                    onValueChange(value.replaceTextRange(block.start, block.end, "", block.start))
                                },
                            )
                        }
                    }
                }
            }
            if (!hasEditableTail) {
                BasicTextField(
                    value = TextFieldValue(""),
                    onValueChange = {
                        if (it.text.isNotEmpty()) {
                            onValueChange(value.copy(text = value.text + "\n" + it.text, selection = TextRange(value.text.length + 1 + it.text.length)))
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(tailFocusRequester)
                        .onFocusChanged { onFocusChange(it.isFocused) },
                )
            }
        }
    }
}

@Composable
private fun NoteTextSegmentEditor(
    block: NoteBlock.TextSegment,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    activeInlineFormats: Set<NoteFormat>,
    findQuery: String,
    selectedFindRange: IntRange?,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BasicTextField(
            value = TextFieldValue(block.text, selection = localSelectionFor(value.selection, block.start, block.end)),
            onValueChange = {
                val formatted = applyActiveInlineFormats(
                    oldValue = TextFieldValue(block.text, selection = localSelectionFor(value.selection, block.start, block.end)),
                    newValue = it,
                    activeInlineFormats = activeInlineFormats,
                )
                onValueChange(value.replaceBlock(block.start, block.end, formatted))
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            visualTransformation = RichNoteVisualTransformation(
                findQuery = findQuery,
                selectedFindRange = selectedFindRange?.takeIf { it.first >= block.start && it.last < block.end }?.let {
                    (it.first - block.start)..(it.last - block.start)
                },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { onFocusChange(it.isFocused) },
        )
    }
}

@Composable
private fun ChecklistBlockEditor(
    block: NoteBlock.Checklist,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = block.checked,
            onCheckedChange = { checked ->
                val token = checklistToken(checked, block.text)
                onValueChange(value.replaceTextRange(block.start, block.end, token, block.start + token.length))
            },
        )
        BasicTextField(
            value = TextFieldValue(block.text, selection = TextRange(block.text.length)),
            onValueChange = { newValue ->
                val token = checklistToken(block.checked, newValue.text)
                onValueChange(
                    value.replaceTextRange(
                        block.start,
                        block.end,
                        token,
                        block.start + token.length,
                        block.start + token.length,
                    ),
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f).onFocusChanged { onFocusChange(it.isFocused) },
        )
    }
}

@Composable
private fun EditableTableBlock(
    block: NoteBlock.Table,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {
    val cells = remember(block.payload) { parseTablePayload(block.payload) }
    fun update(row: Int, column: Int, cellValue: String) {
        val next = cells.map { it.toMutableList() }.toMutableList()
        next[row][column] = cellValue
        val token = tableToken(next)
        onValueChange(value.replaceTextRange(block.start, block.end, token, block.start + token.length))
    }
    fun replaceTable(next: List<List<String>>) {
        val token = tableToken(next)
        onValueChange(value.replaceTextRange(block.start, block.end, token, block.start + token.length))
    }
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { columnIndex, cell ->
                        OutlinedTextField(
                            value = cell,
                            onValueChange = { update(rowIndex, columnIndex, it) },
                            singleLine = true,
                            textStyle = if (rowIndex == 0) {
                                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            modifier = Modifier.weight(1f).onFocusChanged { onFocusChange(it.isFocused) },
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        val columnCount = cells.firstOrNull()?.size ?: 2
                        replaceTable(cells + listOf(List(columnCount) { "" }))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add row")
                }
                TextButton(
                    onClick = {
                        replaceTable(cells.map { row -> row + "" })
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add column")
                }
            }
        }
    }
}

@Composable
private fun FileAttachmentRow(
    attachment: FileAttachmentToken,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val fileExists = remember(attachment.filePath) {
        attachment.filePath.isNotBlank() && File(attachment.filePath).exists()
    }
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = fileExists, onClick = onOpen)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    attachment.displayName.ifBlank { "Attachment" },
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!fileExists) {
                    Text(
                        "File unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete attachment")
            }
        }
    }
}

private fun TextFieldValue.replaceBlock(start: Int, end: Int, replacementValue: TextFieldValue): TextFieldValue =
    replaceTextRange(start, end, replacementValue.text, start + replacementValue.selection.start, start + replacementValue.selection.end)

private fun applyActiveInlineFormats(
    oldValue: TextFieldValue,
    newValue: TextFieldValue,
    activeInlineFormats: Set<NoteFormat>,
): TextFieldValue {
    if (activeInlineFormats.isEmpty()) return newValue
    val inserted = insertedRange(oldValue.text, newValue.text) ?: return newValue
    val insertedText = newValue.text.substring(inserted)
    if (insertedText.isEmpty()) return newValue
    val formatted = formatInsertedText(insertedText, activeInlineFormats)
    if (formatted == insertedText) return newValue
    val nextText = newValue.text.replaceRange(inserted, formatted)
    val shift = formatted.length - insertedText.length
    return newValue.copy(
        text = nextText,
        selection = TextRange(
            (newValue.selection.start + shift).coerceIn(0, nextText.length),
            (newValue.selection.end + shift).coerceIn(0, nextText.length),
        ),
    )
}

private fun insertedRange(oldText: String, newText: String): IntRange? {
    if (newText.length <= oldText.length) return null
    var prefix = 0
    while (prefix < oldText.length && prefix < newText.length && oldText[prefix] == newText[prefix]) prefix++
    var suffix = 0
    while (
        suffix < oldText.length - prefix &&
        suffix < newText.length - prefix &&
        oldText[oldText.lastIndex - suffix] == newText[newText.lastIndex - suffix]
    ) {
        suffix++
    }
    val endExclusive = newText.length - suffix
    return prefix until endExclusive
}

private fun formatInsertedText(text: String, formats: Set<NoteFormat>): String =
    text.split('\n').joinToString("\n") { chunk ->
        if (chunk.isEmpty()) {
            chunk
        } else {
            formats.fold(chunk) { acc, format ->
                when (format) {
                    NoteFormat.Bold -> "**$acc**"
                    NoteFormat.Italic -> "*$acc*"
                    NoteFormat.Underline -> "<u>$acc</u>"
                    NoteFormat.Strikethrough -> "~~$acc~~"
                    else -> acc
                }
            }
        }
    }

private fun checklistToken(checked: Boolean, text: String): String =
    "[[check:${if (checked) "1" else "0"}:${text.replace("]", "")}]]"

private fun fileAttachmentToken(attachment: NoteFileAttachment): String =
    "[[file:${encodeTokenPart(attachment.displayName)}:${encodeTokenPart(attachment.filePath)}:${encodeTokenPart(attachment.mimeType)}]]"

private data class FileAttachmentToken(
    val displayName: String,
    val filePath: String,
    val mimeType: String,
)

private fun parseFileAttachmentToken(payload: String): FileAttachmentToken {
    val parts = payload.split(":")
    return FileAttachmentToken(
        displayName = decodeTokenPart(parts.getOrNull(0).orEmpty()).ifBlank { "Attachment" },
        filePath = decodeTokenPart(parts.getOrNull(1).orEmpty()),
        mimeType = decodeTokenPart(parts.getOrNull(2).orEmpty()).ifBlank { "application/octet-stream" },
    )
}

private fun openFileAttachment(context: Context, attachment: FileAttachmentToken) {
    val file = File(attachment.filePath)
    if (!file.exists()) return
    runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, attachment.mimeType.ifBlank { "application/octet-stream" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open attachment"))
    }
}

private fun deleteFileAttachment(attachment: FileAttachmentToken) {
    if (attachment.filePath.isBlank()) return
    runCatching { File(attachment.filePath).delete() }
}

private fun encodeTokenPart(value: String): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

private fun decodeTokenPart(value: String): String =
    runCatching {
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
    }.getOrDefault(value)

private fun TextFieldValue.replaceTextRange(
    start: Int,
    end: Int,
    replacement: String,
    selectionStart: Int,
    selectionEnd: Int = selectionStart,
): TextFieldValue {
    val newText = text.replaceRange(start, end, replacement)
    return copy(
        text = newText,
        selection = TextRange(selectionStart.coerceIn(0, newText.length), selectionEnd.coerceIn(0, newText.length)),
    )
}

private fun localSelectionFor(selection: TextRange, start: Int, end: Int): TextRange =
    if (selection.min in start..end) {
        TextRange(
            (selection.start - start).coerceIn(0, (end - start).coerceAtLeast(0)),
            (selection.end - start).coerceIn(0, (end - start).coerceAtLeast(0)),
        )
    } else {
        TextRange((end - start).coerceAtLeast(0))
    }

private fun parseTablePayload(payload: String): List<List<String>> {
    val rows = payload.split(";;").map { row -> row.split("|") }.ifEmpty {
        listOf(listOf("Topic", "Note"), listOf("", ""))
    }
    val columnCount = rows.maxOfOrNull { it.size }?.coerceAtLeast(2) ?: 2
    val normalized = rows.map { row -> row + List(columnCount - row.size) { "" } }.toMutableList()
    if (normalized.isEmpty()) normalized += listOf("Topic", "Note")
    if (normalized.size == 1) normalized += List(columnCount) { "" }
    return normalized
}

private fun tableToken(cells: List<List<String>>): String {
    val payload = cells.joinToString(";;") { row ->
        row.joinToString("|") { cell -> cell.replace("]", "").replace(";", ",").replace("|", "/") }
    }
    return "[[table:$payload]]"
}

@Composable
private fun NoteTopBar(
    isLocked: Boolean,
    contentVisible: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onShare: () -> Unit,
    onExportAudio: () -> Unit,
    canExportAudio: Boolean,
    onFind: () -> Unit,
    onLock: () -> Unit,
    onRemoveLock: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreOpen by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        if (isLocked) {
            AssistChip(
                onClick = {},
                label = { Text("Locked") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            )
        }
        Spacer(Modifier.weight(1f))
        if (contentVisible) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo edit")
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo edit")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share note")
            }
            Box {
                IconButton(onClick = { moreOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More tools")
                }
                DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Find in Note") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        onClick = {
                            moreOpen = false
                            onFind()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Export audio") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        enabled = canExportAudio,
                        onClick = {
                            moreOpen = false
                            onExportAudio()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (isLocked) "Remove Lock" else "Lock with password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        onClick = {
                            moreOpen = false
                            if (isLocked) {
                                onRemoveLock()
                            } else {
                                onLock()
                            }
                        },
                    )
                }
            }
            TextButton(onClick = onSave) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun LockedNoteContent(
    title: String,
    createdAt: java.time.Instant?,
    deviceAuthAvailable: Boolean,
    hasCustomPassword: Boolean,
    onViewNote: () -> Unit,
    onEnterPassword: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(44.dp))
        Text(title.ifBlank { "Locked Note" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        createdAt?.let {
            Text(formatDate(it), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            if (deviceAuthAvailable) {
                "This note is locked. Use your phone lock or fingerprint to view it."
            } else {
                "This note is locked."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onViewNote) {
            Text(if (deviceAuthAvailable) "View Note" else "Unlock Note")
        }
        if (hasCustomPassword) {
            TextButton(onClick = onEnterPassword) {
                Text("Use Password")
            }
        }
    }
}

@Composable
private fun FindInNoteToolbar(
    query: String,
    replaceQuery: String,
    replaceMode: Boolean,
    matchCount: Int,
    selectedIndex: Int,
    onReplaceModeChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onReplaceQueryChange: (String) -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var modeMenuOpen by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 5.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                }
                Box {
                    IconButton(onClick = { modeMenuOpen = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = if (replaceMode) "Find and replace" else "Find")
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    DropdownMenu(expanded = modeMenuOpen, onDismissRequest = { modeMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Find") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            onClick = {
                                onReplaceModeChange(false)
                                modeMenuOpen = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Find & Replace") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                onReplaceModeChange(true)
                                modeMenuOpen = false
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    placeholder = { Text("Find in Note") },
                    trailingIcon = {
                        Text(
                            when {
                                query.isBlank() -> ""
                                matchCount == 0 -> "0/0"
                                else -> "${selectedIndex + 1}/$matchCount"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                )
                IconButton(onClick = onPrevious, enabled = matchCount > 0) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous result")
                }
                IconButton(onClick = onNext, enabled = matchCount > 0) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next result")
                }
            }
            if (replaceMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceQueryChange,
                        singleLine = true,
                        placeholder = { Text("Replace") },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onReplaceCurrent, enabled = matchCount > 0) {
                        Text("Replace")
                    }
                    TextButton(onClick = onReplaceAll, enabled = matchCount > 0) {
                        Text("All")
                    }
                }
            }
        }
    }
}

@Composable
private fun WritingAccessoryToolbar(
    isAddingImage: Boolean,
    formatMenuOpen: Boolean,
    attachmentMenuOpen: Boolean,
    onFormatMenuChange: (Boolean) -> Unit,
    onAttachmentMenuChange: (Boolean) -> Unit,
    onFormat: (NoteFormat) -> Unit,
    activeInlineFormats: Set<NoteFormat>,
    onChecklist: () -> Unit,
    onTable: () -> Unit,
    onAudio: () -> Unit,
    isAddingFile: Boolean,
    onAddImage: () -> Unit,
    onAddFile: () -> Unit,
    onDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun chooseFormat(format: NoteFormat) {
        onFormatMenuChange(false)
        onFormat(format)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 5.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box {
                TextButton(onClick = { onFormatMenuChange(true) }) {
                    Text("Aa", fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(expanded = formatMenuOpen, onDismissRequest = { onFormatMenuChange(false) }) {
                    Column(
                        modifier = Modifier
                            .width(292.dp)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FormatPanelButton(
                                label = "Title",
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                onClick = { chooseFormat(NoteFormat.Title) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "Heading",
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                onClick = { chooseFormat(NoteFormat.Heading) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FormatPanelButton(
                                label = "Subheading",
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                onClick = { chooseFormat(NoteFormat.Subheading) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "Body",
                                onClick = { chooseFormat(NoteFormat.Body) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        HorizontalDivider()
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            FormatPanelButton(
                                label = "B",
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                selected = NoteFormat.Bold in activeInlineFormats,
                                onClick = { chooseFormat(NoteFormat.Bold) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "I",
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                                selected = NoteFormat.Italic in activeInlineFormats,
                                onClick = { chooseFormat(NoteFormat.Italic) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "U",
                                textStyle = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.Underline),
                                selected = NoteFormat.Underline in activeInlineFormats,
                                onClick = { chooseFormat(NoteFormat.Underline) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "S",
                                textStyle = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                                selected = NoteFormat.Strikethrough in activeInlineFormats,
                                onClick = { chooseFormat(NoteFormat.Strikethrough) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        HorizontalDivider()
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            FormatPanelButton(
                                label = "Bullet",
                                onClick = { chooseFormat(NoteFormat.BulletList) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "1. List",
                                onClick = { chooseFormat(NoteFormat.NumberedList) },
                                modifier = Modifier.weight(1f),
                            )
                            FormatPanelButton(
                                label = "Quote",
                                onClick = { chooseFormat(NoteFormat.Quote) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onChecklist) {
                Icon(Icons.Default.CheckBox, contentDescription = "Checklist")
            }
            IconButton(onClick = onTable) {
                Icon(Icons.Default.TableChart, contentDescription = "Table")
            }
            Box {
                IconButton(onClick = { onAttachmentMenuChange(true) }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attachments")
                }
                DropdownMenu(expanded = attachmentMenuOpen, onDismissRequest = { onAttachmentMenuChange(false) }) {
                    DropdownMenuItem(
                        text = { Text("Audio") },
                        leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                        onClick = {
                            onAttachmentMenuChange(false)
                            onAudio()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (isAddingImage) "Adding image" else "Photo or image") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                        enabled = !isAddingImage,
                        onClick = {
                            onAttachmentMenuChange(false)
                            onAddImage()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(if (isAddingFile) "Adding file" else "File") },
                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                        enabled = !isAddingFile,
                        onClick = {
                            onAttachmentMenuChange(false)
                            onAddFile()
                        },
                    )
                }
            }
            IconButton(onClick = onDraw) {
                Icon(Icons.Default.Brush, contentDescription = "Draw")
            }
        }
    }
}

@Composable
private fun FormatPanelButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    selected: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                label,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InlineNoteAttachments(
    noteText: String,
    memos: List<VoiceMemoEntity>,
    images: List<NoteImageEntity>,
    playbackState: AudioPlaybackUiState,
    onPlayPauseMemo: (VoiceMemoEntity) -> Unit,
    onSeekMemo: (Int) -> Unit,
    onDeleteAudio: (String) -> Unit,
    onDeleteImage: (String) -> Unit,
) {
    if (memos.isEmpty() && images.isEmpty()) return
    val imageById = images.associateBy { it.id }
    val memoById = memos.associateBy { it.id }
    val inlineAttachments = attachmentTokenRegex.findAll(noteText)
        .map { InlineAttachment(type = it.groupValues[1], id = it.groupValues[2]) }
        .filter { token ->
            when (token.type) {
                "audio" -> memoById.containsKey(token.id)
                "image", "drawing" -> imageById.containsKey(token.id)
                else -> false
            }
        }
        .toList()
    val inlineIds = inlineAttachments.map { it.id }.toSet()
    val fallbackAttachments =
        memos.filterNot { it.id in inlineIds }.map { InlineAttachment("audio", it.id) } +
            images.filterNot { it.id in inlineIds }.map { InlineAttachment("image", it.id) }
    val attachments = inlineAttachments + fallbackAttachments
    if (attachments.isEmpty()) return
    Spacer(Modifier.height(18.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Inserted in note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        attachments.forEachIndexed { index, attachment ->
            when (attachment.type) {
                "audio" -> memoById[attachment.id]?.let { memo ->
                    MemoRow(
                        index = memos.indexOfFirst { it.id == memo.id }.takeIf { it >= 0 }?.plus(1) ?: (index + 1),
                        memo = memo,
                        playbackState = playbackState,
                        onPlayPause = { onPlayPauseMemo(memo) },
                        onSeek = onSeekMemo,
                        onDeleteAudio = { onDeleteAudio(memo.id) },
                    )
                }
                "image", "drawing" -> imageById[attachment.id]?.let { image ->
                    NoteImageRow(
                        image = image,
                        onDelete = { onDeleteImage(image.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordLockDialog(
    deviceAuthAvailable: Boolean,
    onDismiss: () -> Unit,
    onUseDeviceAuth: () -> Unit,
    onLock: (String) -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var useCustomPassword by rememberSaveable { mutableStateOf(!deviceAuthAvailable) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lock Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!useCustomPassword) {
                    Text("Use your phone password, PIN, pattern, or fingerprint to lock and unlock this note.")
                    TextButton(onClick = { useCustomPassword = true }) {
                        Text("Use Custom Password Instead")
                    }
                } else {
                    Text("Create a custom password for this note.")
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = ""
                        },
                        singleLine = true,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = {
                            confirm = it
                            error = ""
                        },
                        singleLine = true,
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    if (deviceAuthAvailable) {
                        TextButton(onClick = { useCustomPassword = false }) {
                            Text("Use Phone Lock Instead")
                        }
                    }
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!useCustomPassword) {
                        onUseDeviceAuth()
                    } else {
                        error = when {
                            password.length < 4 -> "Use at least 4 characters."
                            password != confirm -> "Passwords do not match."
                            else -> ""
                        }
                        if (error.isBlank()) onLock(password)
                    }
                },
            ) {
                Text(if (useCustomPassword) "Lock" else "Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun PasswordUnlockDialog(
    hasCustomPassword: Boolean,
    onDismiss: () -> Unit,
    onUseDeviceAuth: () -> Unit,
    onUnlock: (String, (String) -> Unit) -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onUseDeviceAuth) {
                    Text("Use Phone Lock or Fingerprint")
                }
                if (hasCustomPassword) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = ""
                        },
                        singleLine = true,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                } else {
                    Text("This note uses your phone lock. If it is unavailable, set up a screen lock or fingerprint in system settings.")
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!hasCustomPassword) {
                        onUseDeviceAuth()
                    } else if (password.isBlank()) {
                        error = "Password is required."
                    } else {
                        onUnlock(password) { message -> error = message }
                    }
                },
            ) {
                Text(if (hasCustomPassword) "Unlock" else "Use Phone Lock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun FloatingRecorder(
    recordState: RecordUiState,
    memoCount: Int,
    totalDuration: Long,
    hasRecordPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onOpenAudioList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatingActionButton(
                onClick = {
                    if (!hasRecordPermission) {
                        onRequestPermission()
                    } else if (recordState.isRecording) {
                        onStop()
                    } else {
                        onStart()
                    }
                },
                containerColor = if (recordState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(58.dp),
            ) {
                Icon(
                    if (recordState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (recordState.isRecording) "Stop recording" else "Record audio",
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (recordState.isRecording) "Recording audio" else "Audio recorder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "$memoCount saved - ${formatDuration(totalDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!hasRecordPermission) {
                    Text(
                        "Microphone permission needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (recordState.errorMessage.isNotBlank()) {
                    Text(
                        recordState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (recordState.isSaving) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }
            if (recordState.isRecording) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            } else if (memoCount > 0) {
                IconButton(onClick = onOpenAudioList) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Show voice memos")
                }
            }
        }
    }
}

private data class SketchStroke(
    val points: List<Offset>,
)

@Composable
private fun DrawingPadDialog(
    onDismiss: () -> Unit,
    onSave: (ByteArray) -> Unit,
) {
    val strokes = remember { mutableStateListOf<SketchStroke>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Drawing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke = listOf(offset)
                                    error = ""
                                },
                                onDrag = { change, _ ->
                                    currentStroke = currentStroke + change.position
                                },
                                onDragEnd = {
                                    if (currentStroke.size > 1) {
                                        strokes.add(SketchStroke(currentStroke))
                                    }
                                    currentStroke = emptyList()
                                },
                                onDragCancel = {
                                    currentStroke = emptyList()
                                },
                            )
                        },
                ) {
                    val allStrokes = strokes + listOfNotNull(currentStroke.takeIf { it.size > 1 }?.let { SketchStroke(it) })
                    allStrokes.forEach { stroke ->
                        stroke.points.zipWithNext().forEach { (start, end) ->
                            drawLine(
                                color = androidx.compose.ui.graphics.Color(0xFF202124),
                                start = start,
                                end = end,
                                strokeWidth = 5.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (strokes.isEmpty()) {
                        error = "Draw something before saving."
                    } else {
                        onSave(renderSketchToPng(strokes, canvasSize))
                    }
                },
            ) {
                Text("Insert")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                    },
                    enabled = strokes.isNotEmpty(),
                ) {
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

private fun renderSketchToPng(strokes: List<SketchStroke>, canvasSize: IntSize): ByteArray {
    val sourceWidth = canvasSize.width.takeIf { it > 0 } ?: 1080
    val sourceHeight = canvasSize.height.takeIf { it > 0 } ?: 720
    val width = 1080
    val height = (width * sourceHeight.toFloat() / sourceWidth.toFloat()).toInt().coerceAtLeast(480)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)
    val scaleX = width.toFloat() / sourceWidth.toFloat()
    val scaleY = height.toFloat() / sourceHeight.toFloat()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(32, 33, 36)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 8f
    }
    strokes.forEach { stroke ->
        if (stroke.points.size < 2) return@forEach
        val path = AndroidPath().apply {
            val first = stroke.points.first()
            moveTo(first.x * scaleX, first.y * scaleY)
            stroke.points.drop(1).forEach { point ->
                lineTo(point.x * scaleX, point.y * scaleY)
            }
        }
        canvas.drawPath(path, paint)
    }
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.toByteArray()
    }
}

@Composable
private fun ActiveConversationRow(
    conversation: ConversationEntity,
    onContinue: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active conversation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Continue the memo-question loop before writing your note.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Return to conversation")
            }
            Spacer(Modifier.height(6.dp))
            Text(
                formatDate(conversation.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ConversationHistoryRow(
    summary: ConversationSummaryEntity,
    onClick: () -> Unit,
) {
    val isLocked = summary.conversation.isLocked
    val note = if (isLocked) "" else plainNoteText(summary.conversation.finalNote)
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        summary.conversation.title.ifBlank { "Untitled note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${summary.memoCount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                formatDate(summary.conversation.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLocked) {
                Spacer(Modifier.height(8.dp))
                Text("Locked Note", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(note, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun QuestionPanel(label: String? = null) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            if (label != null) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(LISTENING_QUESTION, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WordPrinciplesPanel() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Words are important to you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SupportPanel(message: String) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:988")))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Call 988")
                }
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:988")))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Text 988")
                }
            }
        }
    }
}

private fun containsSelfHarmThought(text: String): Boolean {
    val lowered = text.lowercase()
    val phrases = listOf(
        "kill myself",
        "end my life",
        "want to die",
        "hurt myself",
        "self harm",
        "self-harm",
        "suicide",
        "suicidal",
        "cut myself",
        "i don't want to live",
        "i do not want to live",
    )
    return phrases.any { lowered.contains(it) }
}

private fun countMatches(text: String, query: String): Int {
    val clean = query.trim()
    if (clean.isEmpty()) return 0
    return findVisibleMatchRanges(text, query).size
}

private fun findVisibleMatchRanges(text: String, query: String): List<IntRange> {
    val clean = query.trim()
    if (clean.isEmpty()) return emptyList()
    val visible = visibleNoteTextWithOriginalOffsets(text)
    return Regex(Regex.escape(clean), RegexOption.IGNORE_CASE).findAll(visible.text).mapNotNull { match ->
        val originalStart = visible.originalOffsets.getOrNull(match.range.first) ?: return@mapNotNull null
        val originalEnd = visible.originalOffsets.getOrNull(match.range.last) ?: return@mapNotNull null
        originalStart..originalEnd
    }.toList()
}

private data class VisibleNoteText(
    val text: String,
    val originalOffsets: List<Int>,
)

private fun visibleNoteTextWithOriginalOffsets(source: String): VisibleNoteText {
    val visible = StringBuilder()
    val offsets = mutableListOf<Int>()
    fun appendChar(index: Int) {
        visible.append(source[index])
        offsets += index
    }
    var lineStart = 0
    while (lineStart < source.length) {
        val lineEnd = source.indexOf('\n', lineStart).let { if (it == -1) source.length else it }
        val line = source.substring(lineStart, lineEnd)
        val prefixLength = paragraphPrefixLength(line)
        var index = lineStart + prefixLength
        while (index < lineEnd) {
            when {
                attachmentTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < lineEnd } != null -> {
                    index = requireNotNull(attachmentTokenRegex.find(source, index)).range.last + 1
                }
                tableTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < lineEnd } != null -> {
                    index = requireNotNull(tableTokenRegex.find(source, index)).range.last + 1
                }
                source.startsWith("**", index) -> index += 2
                source.startsWith("~~", index) -> index += 2
                source.startsWith("<u>", index) -> index += 3
                source.startsWith("</u>", index) -> index += 4
                source[index] == '*' -> index += 1
                else -> {
                    appendChar(index)
                    index++
                }
            }
        }
        if (lineEnd < source.length) {
            appendChar(lineEnd)
            lineStart = lineEnd + 1
        } else {
            lineStart = lineEnd
        }
    }
    return VisibleNoteText(visible.toString(), offsets)
}

private fun shareCurrentNote(
    context: Context,
    title: String,
    note: String,
    imageCount: Int,
    memoCount: Int,
) {
    val attachmentSummary = buildList {
        if (imageCount > 0) add("$imageCount image/drawing ${if (imageCount == 1) "attachment" else "attachments"}")
        if (memoCount > 0) add("$memoCount audio ${if (memoCount == 1) "memo" else "memos"}")
    }
    val body = buildString {
        val cleanTitle = title.trim()
        if (cleanTitle.isNotEmpty()) {
            appendLine(cleanTitle)
            appendLine()
        }
        append(note.trim())
        if (attachmentSummary.isNotEmpty()) {
            if (isNotEmpty()) appendLine()
            appendLine()
            append("Attachments in My Words, My Way: ")
            append(attachmentSummary.joinToString(", "))
        }
    }.ifBlank { "Untitled note" }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Note" })
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "Share note"))
}

@Composable
private fun MemoRow(
    index: Int,
    memo: VoiceMemoEntity,
    playbackState: AudioPlaybackUiState,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onDeleteAudio: () -> Unit,
    showDeleteIcon: Boolean = true,
) {
    val isActive = playbackState.memoId == memo.id
    val canPlay = memo.audioPath != null
    val durationMillis = when {
        isActive && playbackState.durationMillis > 0 -> playbackState.durationMillis.toLong()
        else -> memo.durationMillis ?: 0L
    }
    val positionMillis = if (isActive) playbackState.positionMillis.coerceAtLeast(0) else 0
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (canPlay) {
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            if (isActive && playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isActive && playbackState.isPlaying) "Pause memo audio" else "Play memo audio",
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Memo $index", fontWeight = FontWeight.Medium)
                    Text(
                        when {
                            memo.audioPath != null -> "${formatTime(memo.createdAt)} - ${formatDuration(durationMillis)}"
                            else -> "${formatTime(memo.createdAt)} - audio deleted"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showDeleteIcon && memo.audioPath != null) {
                    IconButton(onClick = onDeleteAudio) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete memo audio")
                    }
                }
            }
            if (canPlay) {
                Slider(
                    value = positionMillis.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..durationMillis.coerceAtLeast(1L).toFloat(),
                    enabled = isActive,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(positionMillis.toLong()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatDuration(durationMillis), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isActive && playbackState.errorMessage.isNotBlank()) {
                Text(playbackState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioMemoListSheet(
    memos: List<VoiceMemoEntity>,
    playbackState: AudioPlaybackUiState,
    onDismiss: () -> Unit,
    onPlayPause: (VoiceMemoEntity) -> Unit,
    onSeek: (Int) -> Unit,
    onDeleteAudio: (VoiceMemoEntity) -> Unit,
) {
    var memoPendingDelete by remember { mutableStateOf<VoiceMemoEntity?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Voice memos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${memos.size} saved - ${formatDuration(memos.sumOf { it.durationMillis ?: 0L })}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (memos.isEmpty()) {
                Text("No saved audio in this note.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                memos.forEachIndexed { index, memo ->
                    if (memo.audioPath != null) {
                        SwipeToDeleteRow(
                            contentDescription = "Delete memo audio",
                            onDelete = { memoPendingDelete = memo },
                        ) {
                            MemoRow(
                                index = index + 1,
                                memo = memo,
                                playbackState = playbackState,
                                onPlayPause = { onPlayPause(memo) },
                                onSeek = onSeek,
                                onDeleteAudio = { memoPendingDelete = memo },
                                showDeleteIcon = false,
                            )
                        }
                    } else {
                        MemoRow(
                            index = index + 1,
                            memo = memo,
                            playbackState = playbackState,
                            onPlayPause = { onPlayPause(memo) },
                            onSeek = onSeek,
                            onDeleteAudio = {},
                            showDeleteIcon = false,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    memoPendingDelete?.let { memo ->
        DeleteMemoAudioConfirmationDialog(
            memo = memo,
            onDismiss = { memoPendingDelete = null },
            onDelete = {
                memoPendingDelete = null
                onDeleteAudio(memo)
            },
        )
    }
}

@Composable
private fun DeleteMemoAudioConfirmationDialog(
    memo: VoiceMemoEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this voice memo audio?") },
        text = {
            Text(
                "This deletes Memo ${formatTime(memo.createdAt)} audio from this device. The note and any text in the note are kept.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SuggestionRow(
    suggestion: NounSuggestionEntity,
    onKeep: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(suggestion.suggestedNoun, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(suggestion.visibleReason)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, label = { Text(suggestion.sceneType) })
                AssistChip(onClick = {}, label = { Text(suggestion.confidence) })
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onKeep, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Keep")
                }
                OutlinedButton(onClick = onRename, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Rename")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun WordRow(
    item: NounWithLinksEntity,
    allNouns: List<NounEntity>,
    onRename: () -> Unit,
    onMerge: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.noun.noun, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Linked notes:", fontWeight = FontWeight.Medium)
            if (item.links.isEmpty()) {
                Text("No linked notes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                item.links.forEach { link ->
                    Text("- ${link.visibleNoteExcerpt}", modifier = Modifier.padding(top = 6.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onRename, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Rename")
                }
                OutlinedButton(
                    onClick = onMerge,
                    enabled = allNouns.size > 1,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Merge")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun NoteImageRow(
    image: NoteImageEntity,
    onDelete: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Box {
            val bitmap by rememberImageBitmap(image.imagePath)
            if (bitmap != null) {
                Image(
                    bitmap = requireNotNull(bitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.55f)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.55f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Image unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape,
                    ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete image")
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: ConversationEntity,
    images: List<NoteImageEntity>,
    memoCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(formatDate(note.createdAt), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (note.isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(
                    note.title.ifBlank { "Untitled note" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            val displayNote = if (note.isLocked) "" else plainNoteText(note.finalNote)
            if (note.isLocked) {
                Spacer(Modifier.height(8.dp))
                Text("Locked Note", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (displayNote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(displayNote, maxLines = 5, overflow = TextOverflow.Ellipsis)
            }
            if (images.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    images.take(3).forEach { image ->
                        NoteImageThumb(image.imagePath)
                    }
                    if (images.size > 3) {
                        Text("+${images.size - 3}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (memoCount > 0) {
                Spacer(Modifier.height(10.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("$memoCount audio ${if (memoCount == 1) "memo" else "memos"}") },
                    leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun NoteImageThumb(imagePath: String) {
    val bitmap by rememberImageBitmap(imagePath)
    Surface(shape = RoundedCornerShape(6.dp), tonalElevation = 1.dp) {
        if (bitmap != null) {
            Image(
                bitmap = requireNotNull(bitmap),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(58.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun rememberImageBitmap(imagePath: String) = produceState<ImageBitmap?>(initialValue = null, imagePath) {
    value = withContext(Dispatchers.IO) {
        val file = File(imagePath)
        if (!file.exists()) null else BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    }
}

@Composable
private fun ExportDialog(
    title: String,
    includedItems: List<String>,
    excludedItems: List<String>,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("This export includes:")
                includedItems.forEach { item -> Text("- $item") }
                Spacer(Modifier.height(10.dp))
                Text("It does not include:")
                excludedItems.forEach { item -> Text("- $item") }
            }
        },
        confirmButton = {
            TextButton(onClick = onExport) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteAudioConfirmationDialog(
    confirmationText: String,
    onConfirmationTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val requiredText = "DELETE AUDIO"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete saved voice memo audio?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This deletes saved audio files for voice memos that are eligible for storage cleanup. Notes, words, images, and visible note text are kept.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Type $requiredText to confirm.")
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = onConfirmationTextChange,
                    singleLine = true,
                    label = { Text("Confirmation") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                enabled = confirmationText.trim() == requiredText,
            ) {
                Text("Delete audio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteNoteConfirmationDialog(
    note: ConversationEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this note?") },
        text = {
            Text(
                "This deletes \"${note.title.ifBlank { "Untitled note" }}\" and its attachments. Notes are precious, so delete one at a time.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteFolderConfirmationDialog(
    folder: NoteFolderWithCount,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this folder?") },
        text = {
            Text(
                "This deletes the folder \"${folder.folder.name}\" and its ${folder.noteCount} ${if (folder.noteCount == 1) "note" else "notes"}. This cannot be undone.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteKnotConfirmationDialog(
    knot: BorromeanKnotWithWords,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this Borromean knot?") },
        text = {
            Text(
                "This deletes \"${knot.knot.title}\" from the Words page. Linked notes are kept.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteWordConfirmationDialog(
    word: BorromeanWordEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this word?") },
        text = {
            Text(
                "This deletes \"${word.text}\" from the Words page. Words are important, so delete one at a time. Linked notes are kept.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ErrorText(message: String) {
    if (message.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

private fun shareTextFile(context: Context, export: TextExportFile, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
        putExtra(Intent.EXTRA_TEXT, export.text)
        putExtra(Intent.EXTRA_STREAM, export.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun shareAudioExport(context: Context, export: AudioExportFile, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_SUBJECT, export.displayName)
        putExtra(Intent.EXTRA_STREAM, export.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

private data class KnotRingConfig(
    val registerType: String,
    val rotation: Float,
    val color: Color,
)

private data class NoteEditSnapshot(
    val title: String,
    val note: TextFieldValue,
)

private data class BorromeanWordColors(
    val container: Color,
    val content: Color,
    val accent: Color,
    val chipContainer: Color,
    val chipContent: Color,
)

private const val BORROMEAN_REGISTER_OBJECT_A = "object_a"
private const val BORROMEAN_REGISTER_REAL = "real"
private const val BORROMEAN_REGISTER_SYMBOLIC = "symbolic"
private const val BORROMEAN_REGISTER_IMAGINARY = "imaginary"
private const val BORROMEAN_REGISTER_AFFECT = "affect"
private const val BORROMEAN_REGISTER_DESIRE = "desire"

private fun formatDate(instant: java.time.Instant): String = dateFormatter.format(instant)

private fun formatTime(instant: java.time.Instant): String = timeFormatter.format(instant)

private fun Context.deviceAuthAvailable(): Boolean =
    getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

private fun borromeanRegisterLabel(registerType: String): String =
    when (registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> "object a fragments"
        BORROMEAN_REGISTER_REAL -> "Real: things that happened"
        BORROMEAN_REGISTER_SYMBOLIC -> "Symbolic"
        BORROMEAN_REGISTER_IMAGINARY -> "Imaginary: images generated"
        BORROMEAN_REGISTER_AFFECT -> "Personally important words"
        BORROMEAN_REGISTER_DESIRE -> "What I truly want"
        else -> registerType
    }

private fun borromeanRegisterDescription(registerType: String): String =
    when (registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> "object a fragments: gaze, voice, breast, excrement."
        BORROMEAN_REGISTER_REAL -> "You met with each other. Similar things happen between you and your mom that bring you back to old memories of familiarity."
        BORROMEAN_REGISTER_SYMBOLIC -> "The words that are important for you."
        BORROMEAN_REGISTER_IMAGINARY -> "Between what really happened and what you think about this person is the imaginary."
        BORROMEAN_REGISTER_AFFECT -> "Words ordered by personal importance, with emotional attachment that may change as the user speaks more."
        BORROMEAN_REGISTER_DESIRE -> "Words extracted from mother's words and pursued through the user's own words and power."
        else -> ""
    }

private fun borromeanRegisterColor(registerType: String): Color =
    when (registerType) {
        BORROMEAN_REGISTER_REAL -> Color(0xFFFF5D8F)
        BORROMEAN_REGISTER_SYMBOLIC -> Color(0xFF6CE5E8)
        BORROMEAN_REGISTER_IMAGINARY -> Color(0xFFFFD166)
        BORROMEAN_REGISTER_AFFECT -> Color(0xFFFF8F3D)
        BORROMEAN_REGISTER_DESIRE -> Color(0xFF8AE66E)
        else -> Color(0xFFFFFFFF)
    }

private fun borromeanWordColors(word: BorromeanWordEntity): BorromeanWordColors {
    val accent = when (word.registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> when (word.objectPartType?.lowercase(Locale.getDefault())) {
            "gaze" -> Color(0xFFB8C7FF)
            "voice" -> Color(0xFFC9B6FF)
            "breast", "body" -> Color(0xFFFFB8C8)
            "excrement", "gift", "control" -> Color(0xFFFFD7A8)
            else -> Color(0xFFE4C6FF)
        }
        BORROMEAN_REGISTER_AFFECT -> Color(0xFFFFB36B)
        BORROMEAN_REGISTER_DESIRE -> Color(0xFFA9E7A3)
        else -> borromeanRegisterColor(word.registerType)
    }
    val container = when (word.registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> blendWithDarkSurface(accent, 0.32f)
        BORROMEAN_REGISTER_AFFECT -> Color(0xFFFFF1E2)
        BORROMEAN_REGISTER_DESIRE -> Color(0xFFEAF8E6)
        else -> accent.copy(alpha = 0.16f)
    }
    val content = when (word.registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> Color.White
        BORROMEAN_REGISTER_AFFECT -> Color(0xFF3F2410)
        BORROMEAN_REGISTER_DESIRE -> Color(0xFF163917)
        else -> Color(0xFF1F1B24)
    }
    val chipContainer = when (word.registerType) {
        BORROMEAN_REGISTER_OBJECT_A -> blendWithDarkSurface(accent, 0.52f)
        BORROMEAN_REGISTER_AFFECT -> Color(0xFFFFE0BC)
        BORROMEAN_REGISTER_DESIRE -> Color(0xFFD0F0CC)
        else -> accent.copy(alpha = 0.24f)
    }
    return BorromeanWordColors(
        container = container,
        content = content,
        accent = accent,
        chipContainer = chipContainer,
        chipContent = content,
    )
}

private fun blendWithDarkSurface(color: Color, amount: Float): Color {
    val base = Color(0xFF171326)
    val clampedAmount = amount.coerceIn(0f, 1f)
    return Color(
        red = base.red + (color.red - base.red) * clampedAmount,
        green = base.green + (color.green - base.green) * clampedAmount,
        blue = base.blue + (color.blue - base.blue) * clampedAmount,
        alpha = 1f,
    )
}

private fun objectPartLabel(type: String): String =
    when (type.lowercase(Locale.getDefault())) {
        "gaze" -> "gaze / eyes"
        "voice" -> "voice"
        "breast", "body" -> "breast / body"
        "excrement", "gift", "control" -> "excrement / gift / control"
        else -> type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

private data class NoteSearchResult(
    val note: ConversationEntity,
    val matchCount: Int,
    val preview: String,
)

private fun rankedNoteSearchResults(notes: List<ConversationEntity>, query: String): List<NoteSearchResult> {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) {
        return notes.map { note ->
            NoteSearchResult(note = note, matchCount = 0, preview = if (note.isLocked) "" else plainNoteText(note.finalNote))
        }
    }
    val loweredQuery = cleanQuery.lowercase(Locale.getDefault())
    return notes.mapNotNull { note ->
        val plainNote = if (note.isLocked) "" else plainNoteText(note.finalNote)
        val searchable = if (note.isLocked) {
            note.title.lowercase(Locale.getDefault())
        } else {
            listOf(note.title, plainNote).joinToString("\n").lowercase(Locale.getDefault())
        }
        val matchCount = countPlainMatches(searchable, loweredQuery)
        if (matchCount == 0) {
            null
        } else {
            NoteSearchResult(note = note, matchCount = matchCount, preview = searchPreview(plainNote, cleanQuery))
        }
    }.sortedWith(
        compareByDescending<NoteSearchResult> { it.matchCount }
            .thenByDescending { it.note.createdAt },
    )
}

private fun countPlainMatches(text: String, query: String): Int {
    if (query.isBlank()) return 0
    var count = 0
    var start = 0
    while (start <= text.length - query.length) {
        val index = text.indexOf(query, start)
        if (index < 0) break
        count++
        start = index + query.length
    }
    return count
}

private fun searchPreview(plainNote: String, query: String): String {
    if (plainNote.isBlank()) return ""
    val index = plainNote.indexOf(query, ignoreCase = true)
    if (index < 0) return plainNote.lineSequence().firstOrNull().orEmpty().take(160)
    val start = (index - 48).coerceAtLeast(0)
    val end = (index + query.length + 96).coerceAtMost(plainNote.length)
    return buildString {
        if (start > 0) append("...")
        append(plainNote.substring(start, end).replace('\n', ' '))
        if (end < plainNote.length) append("...")
    }
}

private fun formatDuration(durationMillis: Long?): String {
    val millis = durationMillis ?: 0L
    if (millis <= 0L) return "0 min"
    val totalSeconds = ((millis + 500L) / 1000L).coerceAtLeast(1L)
    if (totalSeconds < 60L) return "${totalSeconds}s"
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (seconds == 0L) "${minutes} min" else "%d:%02d".format(minutes, seconds)
}

private fun formatShortDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0s"
    val totalSeconds = ((durationMillis + 500L) / 1000L).coerceAtLeast(1L)
    if (totalSeconds < 60L) return "${totalSeconds}s"
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (seconds == 0L) "${minutes}m" else "${minutes}m ${seconds}s"
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (bytes >= 1024L * 1024L * 1024L) {
        "%.2f GB".format(gb)
    } else {
        "%.0f MB".format(mb)
    }
}
