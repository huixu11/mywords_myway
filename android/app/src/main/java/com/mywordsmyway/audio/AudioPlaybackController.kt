package com.mywordsmyway.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mywordsmyway.data.local.VoiceMemoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

data class AudioPlaybackUiState(
    val memoId: String? = null,
    val isPlaying: Boolean = false,
    val positionMillis: Int = 0,
    val durationMillis: Int = 0,
    val errorMessage: String = "",
)

class AudioPlaybackController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf(AudioPlaybackUiState())
        private set

    private var player: MediaPlayer? = null
    private var progressJob: Job? = null

    fun playOrPause(memo: VoiceMemoEntity) {
        val audioPath = memo.audioPath ?: return
        if (state.memoId == memo.id && state.isPlaying) {
            pause()
            return
        }
        if (state.memoId == memo.id && player != null) {
            resume()
            return
        }
        play(memo.id, audioPath)
    }

    fun seekTo(positionMillis: Int) {
        val activePlayer = player ?: return
        val duration = activePlayer.duration.coerceAtLeast(0)
        val nextPosition = positionMillis.coerceIn(0, duration)
        activePlayer.seekTo(nextPosition)
        state = state.copy(positionMillis = nextPosition, durationMillis = duration)
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        player?.runCatching {
            stop()
            release()
        }
        player = null
        state = AudioPlaybackUiState()
    }

    fun release() {
        stop()
    }

    private fun play(memoId: String, audioPath: String) {
        stop()
        val audioFile = File(audioPath)
        if (!audioFile.exists()) {
            state = AudioPlaybackUiState(errorMessage = "Audio file is missing.")
            return
        }
        runCatching {
            MediaPlayer().apply {
                setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    progressJob?.cancel()
                    state = state.copy(isPlaying = false, positionMillis = state.durationMillis)
                }
                prepare()
                start()
            }
        }.onSuccess { mediaPlayer ->
            player = mediaPlayer
            state = AudioPlaybackUiState(
                memoId = memoId,
                isPlaying = true,
                positionMillis = mediaPlayer.currentPosition,
                durationMillis = mediaPlayer.duration.coerceAtLeast(0),
            )
            startProgressUpdates()
        }.onFailure { error ->
            state = AudioPlaybackUiState(errorMessage = error.message ?: "Could not play audio.")
        }
    }

    private fun pause() {
        player?.pause()
        state = state.copy(isPlaying = false)
        progressJob?.cancel()
        progressJob = null
    }

    private fun resume() {
        player?.start()
        state = state.copy(isPlaying = true, errorMessage = "")
        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val activePlayer = player ?: break
                state = state.copy(
                    positionMillis = activePlayer.currentPosition.coerceAtLeast(0),
                    durationMillis = activePlayer.duration.coerceAtLeast(0),
                )
                delay(250)
            }
        }
    }
}
