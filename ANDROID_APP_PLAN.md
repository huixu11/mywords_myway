# Android App Plan

## Product Goal

Build **My Words, My Way** as a local-first Android app.

The Android app should keep the core story from `KAGGLE_WRITEUP.md`:

- The app does not advise, diagnose, summarize, or interpret during conversation.
- It asks only: "What does that make you think about?"
- The user speaks across multiple voice memos.
- The user writes one note at the end: "What was this conversation about?"
- Gemma extracts candidate concrete nouns from hidden transcripts.
- The user keeps, renames, deletes, merges, or edits nouns.
- Kept nouns link back to user-written notes, not AI-written meanings.
- The app is not psychoanalysis, but it borrows one design lesson from Lacanian practice: the user's own words must be allowed to work.

## Recommended Android Stack

- Language: Kotlin
- UI: Jetpack Compose
- Navigation: Navigation Compose
- Local database: Room
- Settings: DataStore
- Audio recording: Android media APIs
- Background work: WorkManager
- Dependency injection: Hilt or lightweight manual injection
- Async: Kotlin coroutines and Flow

## Architecture

Use a native Android local-first architecture, not the current FastAPI/static prototype.

```text
Compose UI
  -> ViewModel
  -> Use cases
  -> Repositories
  -> Room / local files / model service / billing service
```

The current Python prototype remains useful for:

- hackathon browser demo;
- prompt iteration;
- model API bridge;
- backend logic reference.

The Android app should own:

- recording;
- local storage;
- notes;
- noun review;
- search;
- always-available note and voice reflection creation;
- 1 GB cleanup policy.

## Model Integration Phases

### Phase 1: Demo Android App

Use the Android app for the complete product flow and call one model interface:

```kotlin
interface ModelService {
    suspend fun transcribe(audioPath: String): String
    suspend fun safetyCheck(text: String): SafetyResult
    suspend fun extractNouns(transcript: String, existingNouns: List<String>): NounExtractionResult
}
```

Implementations:

- `MockModelService` for reliable demos.
- `HttpModelService` to call the existing FastAPI prototype or a local Gemma server.

### Phase 2: Local-First Production

Replace or supplement `HttpModelService` with:

- on-device Gemma if the target device can support it;
- local server mode for development;
- private user-controlled endpoint if needed.

The UI and database should not depend on where Gemma runs.

## Core Screens

1. **Start / Record**
   - Lets the user start a voice reflection at any time.
   - Explains that users can always create notes and voice reflections.
   - Explains that the app preserves words but is not psychoanalysis.

2. **Record**
   - Large record button.
   - Shows only the listening question.
   - Lets the user add multiple voice memos.
   - Shows safety support text if needed: "I am here listen to you."

3. **Write Note**
   - Required before noun extraction.
   - Prompt: "What was this conversation about?"
   - The user writes only what they want to keep.

4. **Review Suggestions**
   - Shows candidate nouns.
   - Actions: Keep, Rename, Delete.
   - Keeping links the noun to the written note for this conversation.

5. **My Words**
   - Shows confirmed nouns.
   - Each noun shows linked user-written notes.
   - No AI meaning.

6. **My Notes**
   - Shows user notes.
   - Search by text and date.
   - Export notes only.

7. **Privacy / Storage**
   - Shows local data size.
   - Explains audio/transcript cleanup after 1 GB.
   - Lets user manually delete audio, transcripts, conversations, notes, nouns, and suggestions.

## Key Product Rules

- Users can always create notes and voice reflections.
- No paywall.
- Conversation can continue during safety risk; the app adds support and resources.
- App still asks the same listening question.
- App is not therapy and not psychoanalysis.
- Hidden audio/transcripts stay local.
- User-visible exports include notes and confirmed nouns only.
- When local app data exceeds 1 GB, delete oldest audio and hidden transcript text first.
- Keep user-written notes, confirmed nouns, and note links unless the user deletes them.

## Android Milestones

1. Build Compose mockups with fake data.
2. Add Room entities and DAO tests.
3. Add audio recording and local file storage.
4. Add conversation loop and required final note.
5. Add mock Gemma noun extraction.
6. Add review suggestions and note-linked nouns.
7. Add search and notes-only export.
8. Keep note and voice reflection creation always available.
9. Add storage cleanup worker.
10. Add optional HTTP model service.
11. Package demo APK.

