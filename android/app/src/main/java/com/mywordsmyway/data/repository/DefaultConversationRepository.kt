package com.mywordsmyway.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.withTransaction
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.local.ConversationEntity
import com.mywordsmyway.data.local.ConversationSummaryEntity
import com.mywordsmyway.data.local.DEFAULT_NOTE_FOLDER_ID
import com.mywordsmyway.data.local.NoteImageEntity
import com.mywordsmyway.data.local.NoteFolderEntity
import com.mywordsmyway.data.local.NoteFolderWithCount
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.PaymentEntity
import com.mywordsmyway.data.local.SafetyEventEntity
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.AddMemoResult
import com.mywordsmyway.data.model.LISTENING_QUESTION
import com.mywordsmyway.data.model.NoteFileAttachment
import com.mywordsmyway.data.model.NounExtractionResult
import com.mywordsmyway.data.model.SUPPORT_MESSAGE
import com.mywordsmyway.data.model.SafetyResult
import com.mywordsmyway.data.model.StartConversationResult
import com.mywordsmyway.data.model.WeeklyAccess
import com.mywordsmyway.model.ModelService
import com.mywordsmyway.storage.plainNoteText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class DefaultConversationRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val modelService: ModelService,
    private val storageRepository: StorageRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ConversationRepository {
    private val conversationDao = database.conversationDao()
    private val memoDao = database.voiceMemoDao()
    private val noteImageDao = database.noteImageDao()
    private val nounDao = database.nounDao()
    private val paymentDao = database.paymentDao()
    private val safetyEventDao = database.safetyEventDao()

    override fun observeWeeklyAccess(): Flow<WeeklyAccess> {
        val weekStart = currentWeekStart()
        return conversationDao.observeFreeConversationsSince(weekStart).map { count ->
            WeeklyAccess(freeConversationAvailable = count == 0)
        }
    }

    override fun observeCurrentConversation(): Flow<ConversationEntity?> =
        conversationDao.observeCurrentConversation()

    override fun observeConversationHistory(): Flow<List<ConversationSummaryEntity>> =
        conversationDao.observeConversationHistory()

    override fun observeConversation(conversationId: String): Flow<ConversationEntity?> =
        conversationDao.observeConversation(conversationId)

    override fun observeMemos(conversationId: String): Flow<List<VoiceMemoEntity>> =
        memoDao.observeMemosForConversation(conversationId)

    override fun observeNoteImages(conversationId: String): Flow<List<NoteImageEntity>> =
        noteImageDao.observeImagesForConversation(conversationId)

    override fun observeNoteFolders(): Flow<List<NoteFolderWithCount>> =
        conversationDao.observeNoteFolders().onStart { ensureDefaultFolder() }

    private suspend fun ensureDefaultFolder() {
        if (conversationDao.countNoteFolders() == 0) {
            conversationDao.insertNoteFolder(
                NoteFolderEntity(
                    id = DEFAULT_NOTE_FOLDER_ID,
                    name = "Notes",
                    createdAt = Instant.EPOCH,
                    sortOrder = 0,
                    isDefault = true,
                ),
            )
        }
    }

    override fun observeNotes(
        folderId: String?,
        query: String,
        startDate: String,
        endDate: String,
    ): Flow<List<ConversationEntity>> =
        conversationDao.observeNotes(
            folderId = folderId,
            query = query.trim(),
            start = parseStartDate(startDate),
            end = parseEndDate(endDate),
        )

    override fun observeNotesInFolder(folderId: String?, startDate: String, endDate: String): Flow<List<ConversationEntity>> =
        conversationDao.observeNotesInFolder(
            folderId = folderId,
            start = parseStartDate(startDate),
            end = parseEndDate(endDate),
        )

    override suspend fun createNoteFolder(name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Folder name is required." }
        val now = clock.instant()
        ensureDefaultFolder()
        conversationDao.insertNoteFolder(
            NoteFolderEntity(
                id = UUID.randomUUID().toString(),
                name = cleanName,
                createdAt = now,
                sortOrder = conversationDao.countNoteFolders(),
                isDefault = false,
            ),
        )
    }

    override suspend fun renameNoteFolder(folderId: String, name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Folder name is required." }
        conversationDao.renameNoteFolder(folderId, cleanName)
    }

    override suspend fun deleteNoteFolder(folderId: String) {
        database.withTransaction {
            conversationDao.moveConversationsToFolder(folderId, DEFAULT_NOTE_FOLDER_ID)
            conversationDao.deleteNoteFolder(folderId)
        }
    }

    override suspend fun moveNoteToFolder(conversationId: String, folderId: String?) {
        conversationDao.updateConversationFolder(conversationId, folderId ?: DEFAULT_NOTE_FOLDER_ID)
    }

    override suspend fun lockNote(conversationId: String, password: String) {
        val cleanPassword = password.trim()
        require(cleanPassword.isNotEmpty()) { "Password is required." }
        val saltBytes = ByteArray(PASSWORD_SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val salt = Base64.getEncoder().encodeToString(saltBytes)
        val hash = hashPassword(cleanPassword, salt)
        conversationDao.lockConversation(conversationId, salt, hash)
    }

    override suspend fun lockNoteWithDeviceAuth(conversationId: String) {
        conversationDao.lockConversationWithDeviceAuth(conversationId)
    }

    override suspend fun removeNoteLock(conversationId: String) {
        conversationDao.unlockConversation(conversationId)
    }

    override suspend fun verifyNotePassword(conversationId: String, password: String): Boolean {
        val conversation = conversationDao.getConversation(conversationId) ?: return false
        val salt = conversation.passwordSalt ?: return false
        val expectedHash = conversation.passwordHash ?: return false
        val actualHash = hashPassword(password.trim(), salt)
        return MessageDigest.isEqual(
            expectedHash.toByteArray(Charsets.UTF_8),
            actualHash.toByteArray(Charsets.UTF_8),
        )
    }

    override suspend fun startConversation(paymentAcknowledged: Boolean): StartConversationResult =
        startConversation(paymentAcknowledged = paymentAcknowledged, folderId = null)

    override suspend fun startConversation(paymentAcknowledged: Boolean, folderId: String?): StartConversationResult {
        val weekStart = currentWeekStart()
        val freeUsed = conversationDao.countFreeConversationsSince(weekStart) > 0
        if (freeUsed && !paymentAcknowledged) {
            error("Payment acknowledgement is required after this week's free conversation.")
        }

        val now = clock.instant()
        ensureDefaultFolder()
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            title = "Untitled reflection",
            finalNote = "",
            safetyStatus = "none",
            paymentStatus = if (freeUsed) "paid_acknowledged" else "free_weekly",
            isFreeWeekly = !freeUsed,
            folderId = folderId ?: DEFAULT_NOTE_FOLDER_ID,
            isLocked = false,
            passwordSalt = null,
            passwordHash = null,
        )
        database.withTransaction {
            conversationDao.insertConversation(conversation)
            if (freeUsed) {
                paymentDao.insertPayment(
                    PaymentEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversation.id,
                        status = "acknowledged",
                        productId = "paid_reflection_acknowledgement",
                        purchaseToken = null,
                        createdAt = now,
                    ),
                )
            }
        }
        return StartConversationResult(conversation = conversation, question = LISTENING_QUESTION)
    }

    override suspend fun addMemo(
        conversationId: String,
        audioPath: String?,
        durationMillis: Long?,
        textFallback: String?,
    ): AddMemoResult {
        val safety = safetyForMemo(audioPath = audioPath, textFallback = textFallback)
        val now = clock.instant()
        val memo = VoiceMemoEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            audioPath = audioPath,
            audioDeletedAt = null,
            durationMillis = durationMillis,
            transcript = null,
            transcriptDeletedAt = null,
            createdAt = now,
            visibleToUser = false,
        )
        database.withTransaction {
            memoDao.insertMemo(memo)
            conversationDao.updateSafetyStatus(conversationId, safety.riskLevel)
            if (safety.riskLevel != "none") {
                safetyEventDao.insertSafetyEvent(
                    SafetyEventEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversationId,
                        memoId = memo.id,
                        riskLevel = safety.riskLevel,
                        riskTypes = safety.riskTypes.joinToString(","),
                        safeUserMessage = safety.safeUserMessage,
                        createdAt = now,
                    ),
                )
            }
        }
        storageRepository.enforceStorageLimit()
        return AddMemoResult(
            memo = memo,
            memoCount = memoDao.countForConversation(conversationId),
            safety = safety,
        )
    }

    override suspend fun addNoteImage(conversationId: String, sourceUri: Uri): NoteImageEntity = withContext(Dispatchers.IO) {
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        val mimeType = context.contentResolver.getType(sourceUri) ?: "image/jpeg"
        val extension = extensionFor(mimeType)
        val outputDir = File(context.filesDir, "note_images").apply { mkdirs() }
        val outputFile = File(outputDir, "$id.$extension")
        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Could not read selected image." }
            outputFile.outputStream().use { output -> input.copyTo(output) }
        }
        val image = NoteImageEntity(
            id = id,
            conversationId = conversationId,
            imagePath = outputFile.absolutePath,
            mimeType = mimeType,
            sortOrder = noteImageDao.countForConversation(conversationId),
            createdAt = now,
        )
        noteImageDao.insertImage(image)
        image
    }

    override suspend fun addDrawingImage(conversationId: String, pngBytes: ByteArray): NoteImageEntity = withContext(Dispatchers.IO) {
        require(pngBytes.isNotEmpty()) { "Draw something before saving." }
        val now = clock.instant()
        val id = UUID.randomUUID().toString()
        val outputDir = File(context.filesDir, "note_images").apply { mkdirs() }
        val outputFile = File(outputDir, "$id.png")
        outputFile.writeBytes(pngBytes)
        val image = NoteImageEntity(
            id = id,
            conversationId = conversationId,
            imagePath = outputFile.absolutePath,
            mimeType = "image/png",
            sortOrder = noteImageDao.countForConversation(conversationId),
            createdAt = now,
        )
        noteImageDao.insertImage(image)
        image
    }

    override suspend fun addNoteFile(conversationId: String, sourceUri: Uri): NoteFileAttachment = withContext(Dispatchers.IO) {
        val displayName = displayNameFor(sourceUri)
        val mimeType = context.contentResolver.getType(sourceUri) ?: "application/octet-stream"
        val outputDir = File(context.filesDir, "note_files/$conversationId").apply { mkdirs() }
        val outputFile = File(outputDir, "${UUID.randomUUID()}_${displayName.safeFileName()}")
        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Could not read selected file." }
            outputFile.outputStream().use { output -> input.copyTo(output) }
        }
        NoteFileAttachment(
            displayName = displayName,
            filePath = outputFile.absolutePath,
            mimeType = mimeType,
        )
    }

    override suspend fun deleteNoteImage(imageId: String) = withContext(Dispatchers.IO) {
        val image = noteImageDao.getImage(imageId) ?: return@withContext
        deleteNoteImageFile(image.imagePath)
        noteImageDao.deleteImage(imageId)
    }

    override suspend fun updateNote(
        conversationId: String,
        title: String,
        finalNote: String,
    ) {
        val cleanNote = finalNote.trim()
        val visibleNote = plainNoteText(cleanNote)
        val cleanTitle = normalizedTitle(title, visibleNote)
        val imageCount = noteImageDao.countForConversation(conversationId)
        val memoCount = memoDao.countForConversation(conversationId)
        require(cleanTitle.isNotEmpty() || cleanNote.isNotEmpty() || imageCount > 0 || memoCount > 0) {
            "Write a note, record audio, or add an image before saving."
        }
        database.withTransaction {
            conversationDao.updateFinalNote(
                conversationId = conversationId,
                finalNote = cleanNote,
                title = cleanTitle.ifBlank { "Untitled note" },
            )
            nounDao.updateVisibleNoteExcerpt(conversationId, visibleNote)
        }
    }

    override suspend fun finishConversation(
        conversationId: String,
        title: String,
        finalNote: String,
    ): List<NounSuggestionEntity> {
        val cleanNote = finalNote.trim()
        val visibleNote = plainNoteText(cleanNote)
        val cleanTitle = normalizedTitle(title, visibleNote)
        val imageCount = noteImageDao.countForConversation(conversationId)
        val memoCount = memoDao.countForConversation(conversationId)
        require(cleanTitle.isNotEmpty() || cleanNote.isNotEmpty() || imageCount > 0 || memoCount > 0) {
            "Write a note, record audio, or add an image before saving."
        }
        val existingNouns = nounDao.getConfirmedNounNames()
        val extractionSource = listOf(cleanTitle, visibleNote).filter { it.isNotBlank() }.joinToString("\n")
        val result = if (extractionSource.isBlank()) {
            NounExtractionResult(emptyList())
        } else {
            modelService.extractNouns(extractionSource, existingNouns)
        }
        val now = clock.instant()
        val suggestions = result.candidateNouns.map { candidate ->
            NounSuggestionEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                suggestedNoun = candidate.noun.uppercase(),
                suggestionType = candidate.suggestion,
                visibleReason = candidate.visibleReason,
                evidenceSpan = candidate.evidenceSpan,
                sceneType = candidate.sceneType,
                confidence = candidate.confidence,
                status = "pending",
                createdAt = now,
            )
        }

        database.withTransaction {
            conversationDao.updateFinalNote(
                conversationId = conversationId,
                finalNote = cleanNote,
                title = cleanTitle.ifBlank { "Untitled note" },
            )
            suggestions.forEach { nounDao.insertSuggestion(it) }
        }
        return suggestions
    }

    private suspend fun safetyForMemo(audioPath: String?, textFallback: String?): SafetyResult =
        runCatching {
            val sourceText = when {
                !textFallback.isNullOrBlank() -> textFallback
                audioPath != null -> modelService.transcribe(audioPath)
                else -> ""
            }
            if (sourceText.isBlank()) {
                noSafetyRisk()
            } else {
                modelService.safetyCheck(sourceText).withSupportMessage()
            }
        }.getOrElse {
            noSafetyRisk()
        }

    private fun SafetyResult.withSupportMessage(): SafetyResult {
        val hasRisk = riskLevel != "none" || riskTypes.any { it.contains("self", ignoreCase = true) }
        if (!hasRisk) return this
        return copy(
            riskTypes = if (riskTypes.isEmpty()) listOf("self_harm") else riskTypes,
            safeUserMessage = SUPPORT_MESSAGE,
        )
    }

    private fun noSafetyRisk(): SafetyResult =
        SafetyResult(
            riskLevel = "none",
            riskTypes = emptyList(),
            shouldStopReflectionLoop = false,
            safeUserMessage = "",
        )

    private fun normalizedTitle(title: String, note: String): String =
        title.trim().ifEmpty { note.lineSequence().firstOrNull().orEmpty().take(60) }

    override suspend fun deleteConversation(conversationId: String) {
        memoDao.getMemosForConversation(conversationId).forEach { memo ->
            if (memo.audioPath != null) {
                storageRepository.deleteMemoAudio(memo.id)
            }
        }
        noteImageDao.getImagesForConversation(conversationId).forEach { deleteNoteImageFile(it.imagePath) }
        conversationDao.deleteConversation(conversationId)
    }

    override suspend fun deleteAllConversations() {
        storageRepository.deleteOldAudioNow()
        noteImageDao.getAllImages().forEach { deleteNoteImageFile(it.imagePath) }
        conversationDao.deleteAllConversations()
    }

    private fun currentWeekStart(): Instant {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(clock)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone)
            .toInstant()
    }

    private fun parseStartDate(value: String): Instant? {
        val clean = value.trim()
        if (clean.isEmpty()) return null
        return runCatching {
            LocalDate.parse(clean).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    }

    private fun parseEndDate(value: String): Instant? {
        val clean = value.trim()
        if (clean.isEmpty()) return null
        return runCatching {
            LocalDate.parse(clean).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    }

    private fun extensionFor(mimeType: String): String =
        when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }

    private fun displayNameFor(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val value = cursor.getString(index)
                    if (!value.isNullOrBlank()) return value
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/').orEmpty().ifBlank { "Attachment" }
    }

    private fun String.safeFileName(): String =
        replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "attachment" }

    private fun deleteNoteImageFile(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists() || !isInsideNoteImageDirectory(file)) return
        file.delete()
    }

    private fun isInsideNoteImageDirectory(file: File): Boolean {
        val root = File(context.filesDir, "note_images").canonicalFile
        val target = file.canonicalFile
        return target.path.startsWith(root.path)
    }

    private fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, PASSWORD_ITERATIONS, PASSWORD_KEY_BITS)
        return try {
            val key = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM).generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(key)
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        const val PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256"
        const val PASSWORD_ITERATIONS = 120_000
        const val PASSWORD_KEY_BITS = 256
        const val PASSWORD_SALT_BYTES = 16
    }
}
