# Android Data Model

## Storage Principle

Room is the source of truth. Audio files live in app-specific local storage. User-visible exports never include hidden transcripts or audio.

```text
Room database:
- conversations
- voice_memos
- notes
- nouns
- noun_links
- noun_suggestions
- safety_events

File storage:
- audio memo files
```

## Room Entities

### ConversationEntity

```kotlin
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val createdAt: Instant,
    val title: String,
    val finalNote: String,
    val safetyStatus: String,
    val folderId: String?,
    val isLocked: Boolean
)
```

### VoiceMemoEntity

```kotlin
@Entity(
    tableName = "voice_memos",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class VoiceMemoEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val audioPath: String?,
    val audioDeletedAt: Instant?,
    val transcript: String?,
    val transcriptDeletedAt: Instant?,
    val createdAt: Instant,
    val visibleToUser: Boolean = false
)
```

### NounEntity

```kotlin
@Entity(tableName = "nouns")
data class NounEntity(
    @PrimaryKey val id: String,
    val noun: String,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### NounLinkEntity

```kotlin
@Entity(
    tableName = "noun_links",
    foreignKeys = [
        ForeignKey(
            entity = NounEntity::class,
            parentColumns = ["id"],
            childColumns = ["nounId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nounId"), Index("conversationId")]
)
data class NounLinkEntity(
    @PrimaryKey val id: String,
    val nounId: String,
    val conversationId: String,
    val memoId: String?,
    val transcriptSpan: String?,
    val visibleNoteExcerpt: String,
    val sceneType: String,
    val createdAt: Instant
)
```

### NounSuggestionEntity

```kotlin
@Entity(
    tableName = "noun_suggestions",
    indices = [Index("conversationId")]
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
    val createdAt: Instant
)
```

### SafetyEventEntity

```kotlin
@Entity(tableName = "safety_events")
data class SafetyEventEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val memoId: String?,
    val riskLevel: String,
    val riskTypes: String,
    val safeUserMessage: String,
    val createdAt: Instant
)
```

## Scene Types

```text
current_trigger
with_mom
mom_left
what_mom_wanted
what_child_thought_caused_leaving
unclear
```

## Repository Interfaces

```kotlin
interface ConversationRepository {
    fun observeCurrentConversation(): Flow<Conversation?>
    suspend fun startConversation(folderId: String? = null): StartConversationResult
    suspend fun addMemo(conversationId: String, audioPath: String?, textFallback: String?)
    suspend fun finishConversation(conversationId: String, finalNote: String): List<NounSuggestion>
}
```

```kotlin
interface WordRepository {
    fun observeNouns(): Flow<List<NounWithLinks>>
    fun observeSuggestions(): Flow<List<NounSuggestion>>
    suspend fun keepSuggestion(suggestionId: String)
    suspend fun renameSuggestion(suggestionId: String, newNoun: String)
    suspend fun rejectSuggestion(suggestionId: String)
}
```

```kotlin
interface StorageRepository {
    fun observeStorageUsage(): Flow<StorageUsage>
    suspend fun deleteMemoAudio(memoId: String)
    suspend fun enforceStorageLimit(maxBytes: Long = 1_073_741_824L)
}
```

## Cleanup Policy

When local app data is larger than 1 GB:

1. Delete oldest memo audio files.
2. Delete oldest hidden transcript text.
3. Keep final user notes.
4. Keep confirmed nouns.
5. Keep note links.
6. Record deletion timestamps.

This matches the product boundary: hidden data can be cleaned, but the user's own notes remain the primary visible record.

## Access Rule

Users can always create notes and voice reflections. There is no paywall.

