package com.mywordsmyway.model

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

const val GEMMA_4_E4B_MODEL_NAME = "gemma-4-E4B-it.litertlm"
const val GEMMA_4_E4B_MODEL_REPO = "litert-community/gemma-4-E4B-it-litert-lm"
const val GEMMA_4_E4B_MODEL_PAGE_URL = "https://huggingface.co/$GEMMA_4_E4B_MODEL_REPO"
const val GEMMA_4_E4B_MODEL_DOWNLOAD_URL = "$GEMMA_4_E4B_MODEL_PAGE_URL/resolve/main/$GEMMA_4_E4B_MODEL_NAME?download=true"

data class ModelSettings(
    val gemmaModelPath: String = "",
) {
    val isConfigured: Boolean = gemmaModelPath.isNotBlank()
    val isLoadable: Boolean = isConfigured && File(gemmaModelPath).isFile
}

data class GemmaModelDownloadProgress(
    val downloadId: Long = 0L,
    val status: Status = Status.Idle,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val reason: String = "",
) {
    enum class Status {
        Idle,
        Pending,
        Running,
        Successful,
        Failed,
    }

    val isActive: Boolean = status == Status.Pending || status == Status.Running
    val percent: Int? = if (totalBytes > 0L) {
        ((downloadedBytes * 100L) / totalBytes).coerceIn(0L, 100L).toInt()
    } else {
        null
    }

    companion object {
        val Idle = GemmaModelDownloadProgress()
    }
}

private val Context.modelSettingsDataStore by preferencesDataStore(name = "model_settings")

class ModelSettingsRepository(
    private val context: Context,
) {
    private val gemmaModelPathKey = stringPreferencesKey("gemma_model_path")
    private val modelDirectory: File
        get() = File(context.getExternalFilesDir(null), "models").apply { mkdirs() }
    private val gemmaModelFile: File
        get() = File(modelDirectory, GEMMA_4_E4B_MODEL_NAME)

    val settings: Flow<ModelSettings> = context.modelSettingsDataStore.data.map { preferences ->
        ModelSettings(gemmaModelPath = preferences[gemmaModelPathKey].orEmpty())
    }

    suspend fun setGemmaModelPath(path: String) {
        context.modelSettingsDataStore.edit { preferences ->
            preferences[gemmaModelPathKey] = path.trim()
        }
    }

    suspend fun importGemmaModel(sourceUri: Uri): String = withContext(Dispatchers.IO) {
        val target = gemmaModelFile
        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Could not open selected Gemma model file." }
            target.outputStream().use { output ->
                input.copyTo(output, bufferSize = 1024 * 1024)
            }
        }
        setGemmaModelPath(target.absolutePath)
        target.absolutePath
    }

    suspend fun startGemmaModelDownload(): Long {
        val target = gemmaModelFile
        val request = DownloadManager.Request(Uri.parse(GEMMA_4_E4B_MODEL_DOWNLOAD_URL))
            .setTitle("Gemma 4 E4B model")
            .setDescription("Downloading $GEMMA_4_E4B_MODEL_NAME for My Words My Way")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationUri(Uri.fromFile(target))
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val downloadId = downloadManager.enqueue(request)
        setGemmaModelPath(target.absolutePath)
        return downloadId
    }

    fun observeGemmaModelDownload(downloadId: Long): Flow<GemmaModelDownloadProgress> = flow {
        if (downloadId == 0L) {
            emit(GemmaModelDownloadProgress.Idle)
            return@flow
        }
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        while (true) {
            val progress = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                if (!cursor.moveToFirst()) {
                    GemmaModelDownloadProgress(
                        downloadId = downloadId,
                        status = GemmaModelDownloadProgress.Status.Failed,
                        reason = "The Gemma 4 E4B download could not be found.",
                    )
                } else {
                    cursor.toGemmaDownloadProgress(downloadId)
                }
            }
            emit(progress)
            if (!progress.isActive) break
            delay(1_000L)
        }
    }
}

private fun android.database.Cursor.toGemmaDownloadProgress(downloadId: Long): GemmaModelDownloadProgress {
    val status = when (getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
        DownloadManager.STATUS_PENDING -> GemmaModelDownloadProgress.Status.Pending
        DownloadManager.STATUS_RUNNING -> GemmaModelDownloadProgress.Status.Running
        DownloadManager.STATUS_SUCCESSFUL -> GemmaModelDownloadProgress.Status.Successful
        DownloadManager.STATUS_FAILED -> GemmaModelDownloadProgress.Status.Failed
        else -> GemmaModelDownloadProgress.Status.Idle
    }
    val downloaded = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).coerceAtLeast(0L)
    val total = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).coerceAtLeast(0L)
    val reason = if (status == GemmaModelDownloadProgress.Status.Failed) {
        "Download failed. Open the model page, accept the license if needed, then import the downloaded .litertlm file."
    } else {
        ""
    }
    return GemmaModelDownloadProgress(
        downloadId = downloadId,
        status = status,
        downloadedBytes = downloaded,
        totalBytes = total,
        reason = reason,
    )
}
