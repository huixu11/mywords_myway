package com.mywordsmyway.storage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mywordsmyway.data.local.AppDatabase

class StorageCleanupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        DefaultStorageRepository(applicationContext, database).enforceStorageLimit()
        return Result.success()
    }
}
