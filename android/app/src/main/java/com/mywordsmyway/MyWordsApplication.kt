package com.mywordsmyway

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mywordsmyway.storage.StorageCleanupWorker
import java.util.concurrent.TimeUnit

class MyWordsApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleStorageCleanup()
    }

    private fun scheduleStorageCleanup() {
        val request = PeriodicWorkRequestBuilder<StorageCleanupWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "storage-cleanup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
