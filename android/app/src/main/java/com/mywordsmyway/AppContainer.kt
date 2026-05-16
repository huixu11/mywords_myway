package com.mywordsmyway

import android.content.Context
import com.mywordsmyway.data.local.AppDatabase
import com.mywordsmyway.data.repository.BorromeanCalculationStateRepository
import com.mywordsmyway.data.repository.DefaultConversationRepository
import com.mywordsmyway.data.repository.DefaultWordRepository
import com.mywordsmyway.model.GemmaModelService
import com.mywordsmyway.model.ModelSettingsRepository
import com.mywordsmyway.recorder.AndroidAudioRecorder
import com.mywordsmyway.storage.DefaultStorageRepository
import com.mywordsmyway.storage.NotesExporter

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val borromeanCalculationStateRepository = BorromeanCalculationStateRepository(appContext)
    val modelSettingsRepository = ModelSettingsRepository(appContext)
    val modelService = GemmaModelService(modelSettingsRepository)
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
