package com.mywordsmyway.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mywordsmyway.MainActivity
import com.mywordsmyway.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioPlaybackService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var progressJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play(
                memoId = intent.getStringExtra(EXTRA_MEMO_ID).orEmpty(),
                audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH).orEmpty(),
            )
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
            ACTION_TOGGLE -> if (_state.value.isPlaying) pause() else resume()
            ACTION_SEEK -> seekTo(intent.getIntExtra(EXTRA_POSITION_MILLIS, 0))
            ACTION_STOP -> stopPlayback(stopService = true)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopPlayback(stopService = false)
        super.onDestroy()
    }

    private fun play(memoId: String, audioPath: String) {
        stopPlayback(stopService = false)
        val audioFile = File(audioPath)
        if (memoId.isBlank() || !audioFile.isFile) {
            _state.value = AudioPlaybackUiState(errorMessage = "Audio file is missing.")
            stopSelf()
            return
        }

        _state.value = AudioPlaybackUiState(memoId = memoId, isPlaying = true)
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
        runCatching {
            MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    progressJob?.cancel()
                    _state.value = _state.value.copy(isPlaying = false, positionMillis = _state.value.durationMillis)
                    stopForegroundCompat()
                    stopSelf()
                }
                prepare()
                start()
            }
        }.onSuccess { mediaPlayer ->
            player = mediaPlayer
            _state.value = AudioPlaybackUiState(
                memoId = memoId,
                isPlaying = true,
                positionMillis = mediaPlayer.currentPosition.coerceAtLeast(0),
                durationMillis = mediaPlayer.duration.coerceAtLeast(0),
            )
            startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
            startProgressUpdates()
        }.onFailure { error ->
            _state.value = AudioPlaybackUiState(errorMessage = error.message ?: "Could not play audio.")
            stopPlayback(stopService = true)
        }
    }

    private fun pause() {
        player?.takeIf { it.isPlaying }?.pause()
        _state.value = _state.value.copy(isPlaying = false)
        progressJob?.cancel()
        progressJob = null
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying = false))
    }

    private fun resume() {
        val activePlayer = player ?: return
        activePlayer.start()
        _state.value = _state.value.copy(isPlaying = true, errorMessage = "")
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
        startProgressUpdates()
    }

    private fun seekTo(positionMillis: Int) {
        val activePlayer = player ?: return
        val duration = activePlayer.duration.coerceAtLeast(0)
        val nextPosition = positionMillis.coerceIn(0, duration)
        activePlayer.seekTo(nextPosition)
        _state.value = _state.value.copy(positionMillis = nextPosition, durationMillis = duration)
    }

    private fun stopPlayback(stopService: Boolean) {
        progressJob?.cancel()
        progressJob = null
        player?.runCatching {
            stop()
            release()
        }
        player = null
        _state.value = AudioPlaybackUiState()
        stopForegroundCompat()
        if (stopService) stopSelf()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (true) {
                val activePlayer = player ?: break
                _state.value = _state.value.copy(
                    positionMillis = activePlayer.currentPosition.coerceAtLeast(0),
                    durationMillis = activePlayer.duration.coerceAtLeast(0),
                )
                delay(250)
            }
        }
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val toggleIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Playing voice memo")
            .setContentText("My Words My Way")
            .setContentIntent(openIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                toggleIntent,
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Voice memo playback",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    companion object {
        private const val CHANNEL_ID = "voice_memo_playback"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_PLAY = "com.mywordsmyway.audio.PLAY"
        private const val ACTION_PAUSE = "com.mywordsmyway.audio.PAUSE"
        private const val ACTION_RESUME = "com.mywordsmyway.audio.RESUME"
        private const val ACTION_TOGGLE = "com.mywordsmyway.audio.TOGGLE"
        private const val ACTION_SEEK = "com.mywordsmyway.audio.SEEK"
        private const val ACTION_STOP = "com.mywordsmyway.audio.STOP"
        private const val EXTRA_MEMO_ID = "memo_id"
        private const val EXTRA_AUDIO_PATH = "audio_path"
        private const val EXTRA_POSITION_MILLIS = "position_millis"

        private val _state = MutableStateFlow(AudioPlaybackUiState())
        val state: StateFlow<AudioPlaybackUiState> = _state

        fun play(context: Context, memoId: String, audioPath: String) {
            start(context, ACTION_PLAY) {
                putExtra(EXTRA_MEMO_ID, memoId)
                putExtra(EXTRA_AUDIO_PATH, audioPath)
            }
        }

        fun pause(context: Context) = start(context, ACTION_PAUSE)

        fun resume(context: Context) = start(context, ACTION_RESUME)

        fun seekTo(context: Context, positionMillis: Int) {
            start(context, ACTION_SEEK) {
                putExtra(EXTRA_POSITION_MILLIS, positionMillis)
            }
        }

        fun stop(context: Context) = start(context, ACTION_STOP)

        private fun start(context: Context, action: String, configure: Intent.() -> Unit = {}) {
            val intent = Intent(context, AudioPlaybackService::class.java).setAction(action).apply(configure)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
