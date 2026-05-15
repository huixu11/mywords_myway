package com.mywordsmyway.audio

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mywordsmyway.data.local.VoiceMemoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

    private val appContext = context.applicationContext
    private var stateJob: Job? = scope.launch {
        AudioPlaybackService.state.collect { playbackState ->
            state = playbackState
        }
    }

    fun playOrPause(memo: VoiceMemoEntity) {
        val audioPath = memo.audioPath ?: return
        if (state.memoId == memo.id && state.isPlaying) {
            AudioPlaybackService.pause(appContext)
            return
        }
        if (state.memoId == memo.id && state.durationMillis > 0) {
            AudioPlaybackService.resume(appContext)
            return
        }
        AudioPlaybackService.play(appContext, memo.id, audioPath)
    }

    fun seekTo(positionMillis: Int) {
        AudioPlaybackService.seekTo(appContext, positionMillis)
    }

    fun stop() {
        AudioPlaybackService.stop(appContext)
    }

    fun release() {
        stateJob?.cancel()
        stateJob = null
    }
}
