package com.mywordsmyway.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

data class RecordedAudio(
    val path: String,
    val durationMillis: Long,
)

class AndroidAudioRecorder(
    private val context: Context,
) {
    private var recorder: MediaRecorder? = null
    private var activeFile: File? = null
    private var startedAtMillis: Long = 0L

    fun start(): String {
        check(recorder == null) { "A recording is already in progress." }
        val outputFile = File(context.filesDir, "audio/${UUID.randomUUID()}.m4a")
        outputFile.parentFile?.mkdirs()
        val mediaRecorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        activeFile = outputFile
        startedAtMillis = System.currentTimeMillis()
        return outputFile.absolutePath
    }

    fun stop(): RecordedAudio? {
        val currentRecorder = recorder ?: return null
        val file = activeFile
        return try {
            currentRecorder.stop()
            file?.let {
                RecordedAudio(
                    path = it.absolutePath,
                    durationMillis = (System.currentTimeMillis() - startedAtMillis).coerceAtLeast(0L),
                )
            }
        } catch (_: RuntimeException) {
            file?.delete()
            null
        } finally {
            currentRecorder.release()
            recorder = null
            activeFile = null
            startedAtMillis = 0L
        }
    }

    fun cancel() {
        val currentRecorder = recorder ?: return
        val file = activeFile
        runCatching { currentRecorder.stop() }
        currentRecorder.release()
        file?.delete()
        recorder = null
        activeFile = null
        startedAtMillis = 0L
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
}
