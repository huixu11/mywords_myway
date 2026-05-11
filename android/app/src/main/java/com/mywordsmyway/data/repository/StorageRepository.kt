package com.mywordsmyway.data.repository

import com.mywordsmyway.data.model.StorageUsage
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun observeStorageUsage(): Flow<StorageUsage>
    suspend fun deleteMemoAudio(memoId: String)
    suspend fun deleteOldAudioNow()
    suspend fun deleteAllHiddenTranscripts()
    suspend fun enforceStorageLimit(maxBytes: Long = 1_073_741_824L)
}
