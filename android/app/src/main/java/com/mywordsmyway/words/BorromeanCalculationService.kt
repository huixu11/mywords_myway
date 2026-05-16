package com.mywordsmyway.words

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mywordsmyway.MainActivity
import com.mywordsmyway.R

class BorromeanCalculationService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCalculationForeground()
            ACTION_STOP -> stopCalculationForeground()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopCalculationForeground()
        super.onDestroy()
    }

    private fun startCalculationForeground() {
        runCatching {
            acquireWakeLock()
            startForeground(NOTIFICATION_ID, buildNotification())
        }.onFailure {
            wakeLock?.takeIf { lock -> lock.isHeld }?.release()
            wakeLock = null
            stopSelf()
        }
    }

    private fun stopCalculationForeground() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        stopForegroundCompat()
        stopSelf()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWordsMyWay:GemmaWords")
            .apply { acquire(60 * 60 * 1000L) }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Calculating Words with Gemma")
            .setContentText("Keeping the calculation active in the background.")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gemma word calculation",
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
        private const val CHANNEL_ID = "gemma_word_calculation"
        private const val NOTIFICATION_ID = 2002
        private const val ACTION_START = "com.mywordsmyway.words.START_CALCULATION"
        private const val ACTION_STOP = "com.mywordsmyway.words.STOP_CALCULATION"

        fun start(context: Context): Result<Unit> = runCatching {
            val intent = Intent(context, BorromeanCalculationService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context): Result<Unit> = runCatching {
            context.startService(Intent(context, BorromeanCalculationService::class.java).setAction(ACTION_STOP))
        }
    }
}
