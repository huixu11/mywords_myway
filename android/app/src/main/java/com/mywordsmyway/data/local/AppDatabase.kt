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
        NoteFolderEntity::class,
        ConversationEntity::class,
        VoiceMemoEntity::class,
        NoteImageEntity::class,
        NounEntity::class,
        NounLinkEntity::class,
        NounSuggestionEntity::class,
        GemmaNoteProcessingEntity::class,
        BorromeanKnotEntity::class,
        BorromeanWordEntity::class,
        SafetyEventEntity::class,
        PaymentEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
@TypeConverters(InstantConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun voiceMemoDao(): VoiceMemoDao
    abstract fun noteImageDao(): NoteImageDao
    abstract fun nounDao(): NounDao
    abstract fun gemmaNoteProcessingDao(): GemmaNoteProcessingDao
    abstract fun borromeanDao(): BorromeanDao
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_folders (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO note_folders (id, name, createdAt, sortOrder, isDefault)
                    VALUES ('$DEFAULT_NOTE_FOLDER_ID', 'Notes', 0, 0, 1)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE conversations_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        createdAt INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        finalNote TEXT NOT NULL,
                        safetyStatus TEXT NOT NULL,
                        paymentStatus TEXT NOT NULL,
                        isFreeWeekly INTEGER NOT NULL,
                        folderId TEXT,
                        FOREIGN KEY(folderId) REFERENCES note_folders(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO conversations_new (id, createdAt, title, finalNote, safetyStatus, paymentStatus, isFreeWeekly, folderId)
                    SELECT id, createdAt, title, finalNote, safetyStatus, paymentStatus, isFreeWeekly,
                        CASE WHEN TRIM(finalNote) != '' THEN '$DEFAULT_NOTE_FOLDER_ID' ELSE NULL END
                    FROM conversations
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE conversations")
                db.execSQL("ALTER TABLE conversations_new RENAME TO conversations")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conversations_folderId ON conversations(folderId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conversations ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE conversations ADD COLUMN passwordSalt TEXT")
                db.execSQL("ALTER TABLE conversations ADD COLUMN passwordHash TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS borromean_knots (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS borromean_words (
                        id TEXT NOT NULL PRIMARY KEY,
                        knotId TEXT NOT NULL,
                        text TEXT NOT NULL,
                        registerType TEXT NOT NULL,
                        conversationId TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(knotId) REFERENCES borromean_knots(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(conversationId) REFERENCES conversations(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_knotId ON borromean_words(knotId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_conversationId ON borromean_words(conversationId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_registerType ON borromean_words(registerType)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE borromean_knots ADD COLUMN personName TEXT")
                db.execSQL("ALTER TABLE borromean_knots ADD COLUMN theoryNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE borromean_words ADD COLUMN objectPartType TEXT")
                db.execSQL("ALTER TABLE borromean_words ADD COLUMN emotionalWeight INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE borromean_words ADD COLUMN importanceWeight INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE borromean_words ADD COLUMN desireWeight INTEGER NOT NULL DEFAULT 50")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE borromean_words_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        knotId TEXT,
                        text TEXT NOT NULL,
                        registerType TEXT NOT NULL,
                        objectPartType TEXT,
                        emotionalWeight INTEGER NOT NULL,
                        importanceWeight INTEGER NOT NULL,
                        desireWeight INTEGER NOT NULL,
                        conversationId TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(knotId) REFERENCES borromean_knots(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(conversationId) REFERENCES conversations(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO borromean_words_new (
                        id,
                        knotId,
                        text,
                        registerType,
                        objectPartType,
                        emotionalWeight,
                        importanceWeight,
                        desireWeight,
                        conversationId,
                        createdAt,
                        updatedAt,
                        sortOrder
                    )
                    SELECT
                        id,
                        CASE WHEN registerType IN ('affect', 'desire') THEN NULL ELSE knotId END,
                        text,
                        registerType,
                        objectPartType,
                        emotionalWeight,
                        importanceWeight,
                        desireWeight,
                        conversationId,
                        createdAt,
                        updatedAt,
                        sortOrder
                    FROM borromean_words
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE borromean_words")
                db.execSQL("ALTER TABLE borromean_words_new RENAME TO borromean_words")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_knotId ON borromean_words(knotId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_conversationId ON borromean_words(conversationId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_registerType ON borromean_words(registerType)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recreateBorromeanWordsForGemma(db, sourceColumnsExist = false)
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                recreateBorromeanWordsForGemma(db, sourceColumnsExist = true)
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS gemma_note_processing (
                        conversationId TEXT NOT NULL PRIMARY KEY,
                        processedAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        FOREIGN KEY(conversationId) REFERENCES conversations(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
        }

        private fun recreateBorromeanWordsForGemma(db: SupportSQLiteDatabase, sourceColumnsExist: Boolean) {
            db.execSQL(
                """
                CREATE TABLE borromean_words_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    knotId TEXT,
                    text TEXT NOT NULL,
                    registerType TEXT NOT NULL,
                    objectPartType TEXT,
                    emotionalWeight INTEGER NOT NULL,
                    importanceWeight INTEGER NOT NULL,
                    desireWeight INTEGER NOT NULL,
                    conversationId TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    sortOrder INTEGER NOT NULL,
                    source TEXT NOT NULL,
                    sourceConversationId TEXT,
                    sourceMemoId TEXT,
                    extractionEvidence TEXT,
                    extractedAt INTEGER,
                    FOREIGN KEY(knotId) REFERENCES borromean_knots(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(conversationId) REFERENCES conversations(id) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            val sourceSelect = if (sourceColumnsExist) {
                "source, sourceConversationId, sourceMemoId, extractionEvidence, extractedAt"
            } else {
                "'manual' AS source, NULL AS sourceConversationId, NULL AS sourceMemoId, NULL AS extractionEvidence, NULL AS extractedAt"
            }
            db.execSQL(
                """
                INSERT INTO borromean_words_new (
                    id,
                    knotId,
                    text,
                    registerType,
                    objectPartType,
                    emotionalWeight,
                    importanceWeight,
                    desireWeight,
                    conversationId,
                    createdAt,
                    updatedAt,
                    sortOrder,
                    source,
                    sourceConversationId,
                    sourceMemoId,
                    extractionEvidence,
                    extractedAt
                )
                SELECT
                    id,
                    knotId,
                    text,
                    registerType,
                    objectPartType,
                    emotionalWeight,
                    importanceWeight,
                    desireWeight,
                    conversationId,
                    createdAt,
                    updatedAt,
                    sortOrder,
                    $sourceSelect
                FROM borromean_words
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE borromean_words")
            db.execSQL("ALTER TABLE borromean_words_new RENAME TO borromean_words")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_knotId ON borromean_words(knotId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_conversationId ON borromean_words(conversationId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_borromean_words_registerType ON borromean_words(registerType)")
        }
    }
}
