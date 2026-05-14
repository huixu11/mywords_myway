package com.mywordsmyway.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant

const val DEFAULT_NOTE_FOLDER_ID = "default_notes"

@Entity(tableName = "note_folders")
data class NoteFolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Instant,
    val sortOrder: Int,
    val isDefault: Boolean,
)

@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = NoteFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("folderId")],
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val createdAt: Instant,
    val title: String,
    val finalNote: String,
    val safetyStatus: String,
    val paymentStatus: String,
    val isFreeWeekly: Boolean,
    val folderId: String?,
    val isLocked: Boolean,
    val passwordSalt: String?,
    val passwordHash: String?,
)

@Entity(
    tableName = "voice_memos",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class VoiceMemoEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val audioPath: String?,
    val audioDeletedAt: Instant?,
    val durationMillis: Long?,
    val transcript: String?,
    val transcriptDeletedAt: Instant?,
    val createdAt: Instant,
    val visibleToUser: Boolean = false,
)

@Entity(
    tableName = "note_images",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class NoteImageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val imagePath: String,
    val mimeType: String,
    val sortOrder: Int,
    val createdAt: Instant,
)

@Entity(tableName = "nouns")
data class NounEntity(
    @PrimaryKey val id: String,
    val noun: String,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "noun_links",
    foreignKeys = [
        ForeignKey(
            entity = NounEntity::class,
            parentColumns = ["id"],
            childColumns = ["nounId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("nounId"), Index("conversationId")],
)
data class NounLinkEntity(
    @PrimaryKey val id: String,
    val nounId: String,
    val conversationId: String,
    val memoId: String?,
    val transcriptSpan: String?,
    val visibleNoteExcerpt: String,
    val sceneType: String,
    val createdAt: Instant,
)

@Entity(tableName = "borromean_knots")
data class BorromeanKnotEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val personName: String?,
    val theoryNote: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val sortOrder: Int,
    val isArchived: Boolean,
)

@Entity(
    tableName = "borromean_words",
    foreignKeys = [
        ForeignKey(
            entity = BorromeanKnotEntity::class,
            parentColumns = ["id"],
            childColumns = ["knotId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("knotId"), Index("conversationId"), Index("registerType")],
)
data class BorromeanWordEntity(
    @PrimaryKey val id: String,
    val knotId: String?,
    val text: String,
    val registerType: String,
    val objectPartType: String?,
    val emotionalWeight: Int,
    val importanceWeight: Int,
    val desireWeight: Int,
    val conversationId: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val sortOrder: Int,
)

@Entity(
    tableName = "noun_suggestions",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class NounSuggestionEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val suggestedNoun: String,
    val suggestionType: String,
    val visibleReason: String,
    val evidenceSpan: String,
    val sceneType: String,
    val confidence: String,
    val status: String,
    val createdAt: Instant,
)

@Entity(
    tableName = "safety_events",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId"), Index("memoId")],
)
data class SafetyEventEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val memoId: String?,
    val riskLevel: String,
    val riskTypes: String,
    val safeUserMessage: String,
    val createdAt: Instant,
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("conversationId")],
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val conversationId: String?,
    val status: String,
    val productId: String,
    val purchaseToken: String?,
    val createdAt: Instant,
)

data class NounWithLinksEntity(
    @Embedded val noun: NounEntity,
    @Relation(parentColumn = "id", entityColumn = "nounId")
    val links: List<NounLinkEntity>,
)

data class BorromeanKnotWithWords(
    @Embedded val knot: BorromeanKnotEntity,
    @Relation(parentColumn = "id", entityColumn = "knotId")
    val words: List<BorromeanWordEntity>,
)

data class NounLinkExportRow(
    val noun: String,
    val conversationId: String,
)

data class ConversationSummaryEntity(
    @Embedded val conversation: ConversationEntity,
    val memoCount: Int,
)

data class NoteFolderWithCount(
    @Embedded val folder: NoteFolderEntity,
    val noteCount: Int,
)
