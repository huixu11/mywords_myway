package com.mywordsmyway

import android.content.Context
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.repository.DefaultConversationRepository
import com.mywordsmyway.data.repository.DefaultWordRepository
import com.mywordsmyway.model.MockModelService
import com.mywordsmyway.recorder.AndroidAudioRecorder
import com.mywordsmyway.storage.DefaultStorageRepository
import com.mywordsmyway.storage.NotesExporter

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val modelService = MockModelService()
    val storageRepository = DefaultStorageRepository(appContext, database)
    val conversationRepository = DefaultConversationRepository(
        context = appContext,
        database = database,
        modelService = modelService,
        storageRepository = storageRepository,
    )
    val wordRepository = DefaultWordRepository(database)
    val audioRecorder = AndroidAudioRecorder(appContext)
    val notesExporter = NotesExporter(appContext, database)
}
