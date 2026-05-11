package com.mywordsmyway.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ConversationEntity::class,
        VoiceMemoEntity::class,
        NoteImageEntity::class,
        NounEntity::class,
        NounLinkEntity::class,
        NounSuggestionEntity::class,
        SafetyEventEntity::class,
        PaymentEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(InstantConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun voiceMemoDao(): VoiceMemoDao
    abstract fun noteImageDao(): NoteImageDao
    abstract fun nounDao(): NounDao
    abstract fun safetyEventDao(): SafetyEventDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_words_my_way.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE voice_memos ADD COLUMN durationMillis INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_images (
                        id TEXT NOT NULL PRIMARY KEY,
                        conversationId TEXT NOT NULL,
                        imagePath TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(conversationId) REFERENCES conversations(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_images_conversationId ON note_images(conversationId)")
            }
        }
    }
}
