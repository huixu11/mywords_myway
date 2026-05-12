package com.mywordsmyway.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
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
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NounEntity
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.LISTENING_QUESTION
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.SUPPORT_MESSAGE
import com.mywordsmyway.storage.plainNoteText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64

@Composable
fun AccessScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    onConversationStarted: (String) -> Unit,
    onConversationSelected: (String) -> Unit,
    onNotNow: () -> Unit,
) {
    val access by viewModel.weeklyAccess.collectAsState()
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
            if (access.freeConversationAvailable) {
                Text("1 free conversation this week", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Record multiple memos in one conversation. After each memo, the app asks one question. When you stop, write your own note.")
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.startConversation(paymentAcknowledged = false)
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
            } else {
                Text("This week's free conversation has been used.", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Text("Paid conversation", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Paying is a boundary. It asks you to slow down and write carefully. It does not make this app psychoanalysis.")
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.startConversation(paymentAcknowledged = true)
                                .onSuccess(onConversationStarted)
                                .onFailure { error = it.message.orEmpty() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Continue with paid conversation")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onNotNow, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Not now")
                }
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
    val totalDuration = remember(memos) { memos.sumOf { it.durationMillis ?: 0L } }
    val nextRound = memos.size + 1
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                "${memos.size} memos saved - ${formatDuration(totalDuration)} recorded - 20-30 min recommended",
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
            MemoRow(index = index + 1, memo = memo, onDeleteAudio = {
                scope.launch { viewModel.deleteMemoAudio(memo.id) }
            })
            if (index < memos.lastIndex) {
                QuestionPanel(label = "After memo ${index + 1}")
            }
        }
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
    val totalDuration = remember(memos) { memos.sumOf { it.durationMillis ?: 0L } }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
    var activeInlineFormatNames by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var loadedConversationId by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var savedNotice by rememberSaveable { mutableStateOf("") }
    var isAddingImage by rememberSaveable { mutableStateOf(false) }
    var isAddingFile by rememberSaveable { mutableStateOf(false) }
    var showDrawingPad by rememberSaveable { mutableStateOf(false) }
    var showFindInNote by rememberSaveable { mutableStateOf(false) }
    var findQuery by rememberSaveable { mutableStateOf("") }
    var findReplaceMode by rememberSaveable { mutableStateOf(false) }
    var replaceQuery by rememberSaveable { mutableStateOf("") }
    var selectedFindIndex by rememberSaveable { mutableStateOf(0) }
    var showLockDialog by rememberSaveable { mutableStateOf(false) }
    var noteLocked by rememberSaveable(conversationId) { mutableStateOf(false) }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasRecordPermission = granted
    }
    fun insertIntoNote(snippet: String) {
        noteField = noteField.insertAtSelection(snippet)
        error = ""
        savedNotice = ""
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
        title = loaded.title.takeUnless { it == "Untitled reflection" || it == "Untitled note" }.orEmpty()
        noteField = TextFieldValue(loaded.finalNote, selection = TextRange(loaded.finalNote.length))
        loadedConversationId = loaded.id
    }
    fun applyFormat(format: NoteFormat) {
        if (format.isInline && noteField.selection.collapsed) {
            activeInlineFormatNames = if (format.name in activeInlineFormatNames) {
                activeInlineFormatNames - format.name
            } else {
                activeInlineFormatNames + format.name
            }
        } else {
            noteField = noteField.applyNoteFormat(format)
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
    BackHandler {
        saveNote(navigateAfterSave = true, allowEmptyBack = true)
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
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = floatingControlsPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NoteTopBar(
                    isLocked = noteLocked,
                    onBack = { saveNote(navigateAfterSave = true, allowEmptyBack = true) },
                    onShare = { shareCurrentNote(context, title, noteField.text, images.size, memos.size) },
                    onFind = {
                        focusManager.clearFocus()
                        showFindInNote = true
                    },
                    onLock = { showLockDialog = true },
                    onSave = { saveNote(navigateAfterSave = true) },
                )
                if (savedNotice.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(savedNotice, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
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
                    onValueChange = {
                        title = it
                        error = ""
                    },
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
                    onValueChange = {
                        noteField = it
                        error = ""
                    },
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
                    onDeleteImage = { imageId -> scope.launch { viewModel.deleteNoteImage(imageId) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = visibleNoteEditorMinHeight),
                )
                ErrorText(error)
            }
        }

        if (showFindInNote) {
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
                        noteField = noteField.replaceTextRange(
                            start = match.first,
                            end = match.last + 1,
                            replacement = replaceQuery,
                            selectionStart = match.first + replaceQuery.length,
                        )
                    }
                },
                onReplaceAll = {
                    if (findMatches.isNotEmpty()) {
                        val nextText = findMatches.asReversed().fold(noteField.text) { text, match ->
                            text.replaceRange(match.first, match.last + 1, replaceQuery)
                        }
                        noteField = noteField.copy(text = nextText, selection = TextRange(nextText.length))
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
        } else if (showWritingToolbar) {
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
        } else {
            FloatingRecorder(
                recordState = recordState,
                memoCount = memos.size,
                totalDuration = totalDuration,
                hasRecordPermission = hasRecordPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onStart = { viewModel.startRecording() },
                onStop = { scope.launch { viewModel.stopRecordingAndSave(conversationId) } },
                onCancel = { viewModel.cancelRecording() },
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

    if (showLockDialog) {
        PasswordLockDialog(
            onDismiss = { showLockDialog = false },
            onLock = {
                noteLocked = true
                savedNotice = "Locked"
                showLockDialog = false
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
) {
    val nouns by viewModel.nouns.collectAsState()
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<NounEntity?>(null) }
    var renameText by rememberSaveable { mutableStateOf("") }
    var mergeTarget by remember { mutableStateOf<NounEntity?>(null) }
    val filtered = remember(nouns, query) {
        val clean = query.trim()
        if (clean.isEmpty()) {
            nouns
        } else {
            nouns.filter { item ->
                item.noun.noun.contains(clean, ignoreCase = true) ||
                    item.links.any { it.visibleNoteExcerpt.contains(clean, ignoreCase = true) }
            }
        }
    }

    Page(contentPadding = contentPadding) {
        item {
            Text("My Words", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            WordPrinciplesPanel()
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("Search words and linked notes") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }
        items(filtered, key = { it.noun.id }) { item ->
            WordRow(
                item = item,
                allNouns = nouns.map { it.noun },
                onRename = {
                    renameTarget = item.noun
                    renameText = item.noun.noun
                },
                onMerge = { mergeTarget = item.noun },
                onDelete = { scope.launch { viewModel.deleteNoun(item.noun.id) } },
            )
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename word") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Word") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { viewModel.renameNoun(target.id, renameText) }
                        renameTarget = null
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            },
        )
    }

    mergeTarget?.let { source ->
        AlertDialog(
            onDismissRequest = { mergeTarget = null },
            title = { Text("Merge word") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Merge ${source.noun} into:")
                    nouns.map { it.noun }.filter { it.id != source.id }.forEach { target ->
                        TextButton(
                            onClick = {
                                scope.launch { viewModel.mergeNoun(source.id, target.id) }
                                mergeTarget = null
                            },
                        ) {
                            Text(target.noun)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mergeTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    onNewNote: (String) -> Unit,
    onEditNote: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var createError by rememberSaveable { mutableStateOf("") }
    val notesFlow = remember(query, startDate, endDate) {
        viewModel.observeNotes(query, startDate, endDate)
    }
    val notes by notesFlow.collectAsState(initial = emptyList())

    Page(contentPadding = contentPadding) {
        item {
            Text("Notes", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        viewModel.startNote()
                            .onSuccess {
                                createError = ""
                                onNewNote(it)
                            }
                            .onFailure { createError = it.message ?: "Could not create note." }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New note")
            }
            ErrorText(createError)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("Search notes") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    singleLine = true,
                    label = { Text("Start date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    singleLine = true,
                    label = { Text("End date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { showExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export notes only")
            }
            Spacer(Modifier.height(16.dp))
        }
        items(notes, key = { it.id }) { note ->
            val noteImages by viewModel.observeNoteImages(note.id).collectAsState(initial = emptyList())
            val noteMemos by viewModel.observeMemos(note.id).collectAsState(initial = emptyList())
            NoteRow(
                note = note,
                images = noteImages,
                memoCount = noteMemos.size,
                onClick = { onEditNote(note.id) },
            )
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = {
                showExportDialog = false
                scope.launch {
                    viewModel.createNotesOnlyExport()
                        .onSuccess { shareTextFile(context, it) }
                }
            },
        )
    }
}

@Composable
fun PrivacyScreen(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
) {
    val usage by viewModel.storageUsage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    val progress = (usage.usedBytes.toFloat() / usage.limitBytes.toFloat()).coerceIn(0f, 1f)

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
                "Audio and note images stay on this device. New notes do not save transcripts. When app data is larger than 1 GB, the oldest audio is deleted. Notes are kept. Saved words are managed in My Words.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            OutlinedButton(
                onClick = { scope.launch { viewModel.deleteOldAudioNow() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete old audio now")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showExportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Export notes only")
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = {
                showExportDialog = false
                scope.launch {
                    viewModel.createNotesOnlyExport()
                        .onSuccess { shareTextFile(context, it) }
                }
            },
        )
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
    onBack: () -> Unit,
    onShare: () -> Unit,
    onFind: () -> Unit,
    onLock: () -> Unit,
    onSave: () -> Unit,
) {
    var moreOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    text = { Text("Lock with password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    onClick = {
                        moreOpen = false
                        onLock()
                    },
                )
            }
        }
        TextButton(onClick = onSave) {
            Text("Save")
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
    onDismiss: () -> Unit,
    onLock: () -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lock with password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    error = when {
                        password.length < 4 -> "Use at least 4 characters."
                        password != confirm -> "Passwords do not match."
                        else -> ""
                    }
                    if (error.isBlank()) onLock()
                },
            ) {
                Text("Lock")
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
    val note = plainNoteText(summary.conversation.finalNote)
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    summary.conversation.title.ifBlank { "Untitled note" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
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
            if (note.isNotBlank()) {
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
    onDeleteAudio: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Memo $index", fontWeight = FontWeight.Medium)
                Text(
                    when {
                        memo.audioPath != null -> "${formatTime(memo.createdAt)} - ${formatDuration(memo.durationMillis)}"
                        else -> "${formatTime(memo.createdAt)} - audio deleted"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (memo.audioPath != null) {
                IconButton(onClick = onDeleteAudio) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete memo audio")
                }
            }
        }
    }
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
            Text(
                note.title.ifBlank { "Untitled note" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val displayNote = plainNoteText(note.finalNote)
            if (displayNote.isNotBlank()) {
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
    onDismiss: () -> Unit,
    onExport: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export notes only?") },
        text = {
            Column {
                Text("This export includes:")
                Text("- your written notes")
                Text("- dates")
                Text("- linked confirmed nouns")
                Spacer(Modifier.height(10.dp))
                Text("It does not include:")
                Text("- audio")
                Text("- note images")
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
private fun ErrorText(message: String) {
    if (message.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

private fun shareTextFile(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export notes only"))
}

private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

private fun formatDate(instant: java.time.Instant): String = dateFormatter.format(instant)

private fun formatTime(instant: java.time.Instant): String = timeFormatter.format(instant)

private fun formatDuration(durationMillis: Long?): String {
    val millis = durationMillis ?: 0L
    if (millis <= 0L) return "0 min"
    val totalSeconds = ((millis + 500L) / 1000L).coerceAtLeast(1L)
    if (totalSeconds < 60L) return "${totalSeconds}s"
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (seconds == 0L) "${minutes} min" else "%d:%02d".format(minutes, seconds)
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
