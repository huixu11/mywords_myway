package com.mywordsmyway.storage

import android.content.Context
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.local.VoiceMemoEntity
import com.mywordsmyway.data.model.StorageUsage
import com.mywordsmyway.data.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Clock

class DefaultStorageRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) : StorageRepository {
    private val memoDao = database.voiceMemoDao()
    private val storageLimitBytes = 1_073_741_824L

    override fun observeStorageUsage(): Flow<StorageUsage> = flow {
        while (currentCoroutineContext().isActive) {
            emit(StorageUsage(usedBytes = calculateUsageBytes(), limitBytes = storageLimitBytes))
            delay(2_000)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteMemoAudio(memoId: String) = withContext(Dispatchers.IO) {
        val memo = memoDao.getMemo(memoId) ?: return@withContext
        deleteAudioFile(memo.audioPath)
        memoDao.markAudioDeleted(memoId, clock.instant())
    }

    override suspend fun deleteOldAudioNow() = withContext(Dispatchers.IO) {
        val now = clock.instant()
        memoDao.getCleanupCandidates()
            .filter { it.audioPath != null }
            .forEach { memo ->
                deleteAudioFile(memo.audioPath)
                memoDao.markAudioDeleted(memo.id, now)
            }
    }

    override suspend fun deleteAllHiddenTranscripts() = withContext(Dispatchers.IO) {
        memoDao.deleteAllHiddenTranscripts(clock.instant())
        compactDatabase()
    }

    override suspend fun enforceStorageLimit(maxBytes: Long) = withContext(Dispatchers.IO) {
        var usage = calculateUsageBytes()
        if (usage <= maxBytes) return@withContext
        val now = clock.instant()
        var transcriptDeleted = false
        for (memo in memoDao.getCleanupCandidates()) {
            if (usage <= maxBytes) break
            if (memo.audioPath != null) {
                deleteAudioFile(memo.audioPath)
                memoDao.markAudioDeleted(memo.id, now)
                usage = calculateUsageBytes()
            }
            if (usage <= maxBytes) break
            if (memo.transcript != null) {
                memoDao.markTranscriptDeleted(memo.id, now)
                transcriptDeleted = true
                usage = calculateUsageBytes()
            }
        }
        if (transcriptDeleted) compactDatabase()
    }

    private fun calculateUsageBytes(): Long {
        val roots = listOf(
            context.filesDir,
            context.cacheDir,
            context.getDatabasePath("my_words_my_way.db"),
        )
        return roots.sumOf { file -> file.sizeOrZero() }
    }

    private fun File.sizeOrZero(): Long {
        if (!exists()) return 0L
        if (isFile) return length()
        return walkTopDown().filter { it.isFile }.sumOf { runCatching { it.length() }.getOrDefault(0L) }
    }

    private fun deleteAudioFile(audioPath: String?) {
        val path = audioPath ?: return
        val file = File(path)
        if (!file.exists() || !isInsideAudioDirectory(file)) return
        file.delete()
    }

    private fun isInsideAudioDirectory(file: File): Boolean {
        val root = File(context.filesDir, "audio").canonicalFile
        val target = file.canonicalFile
        return target.path.startsWith(root.path)
    }

    private fun compactDatabase() {
        runCatching {
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
            database.openHelper.writableDatabase.execSQL("VACUUM")
        }
    }
}
