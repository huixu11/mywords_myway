# My Words, My Way Demo Video Runbook

Goal: create several short clips that can be selected and concatenated into a 3-minute Kaggle video.

## What To Record

Record these clips separately. If one clip is not good, re-record only that clip.

1. `01_opening_people_phone.mp4`, 15-20 seconds
   - Visual: title card or simple phone-in-hand shot.
   - Voiceover: "My Words, My Way is a local-first Android app built with Gemma 4. It is not an AI therapist. It is a private listening tool that helps people preserve their own words."

2. `02_problem_llm_too_much.mp4`, 20-25 seconds
   - Visual: show a generic AI chat screen or title card saying "Too much advice, too quickly".
   - Voiceover: "Most AI reflection apps respond too quickly. They summarize and interpret the user. For intimate reflection, that can make the model sound like an authority."

3. `03_record_note_flow.mp4`, 30-35 seconds
   - Visual: open the app, start a voice reflection, show the listening question, then open/write a note.
   - Voiceover: "Here the user records voice memos and writes their own note. Users can always create notes. There is no paywall."

4. `04_gemma_calculation.mp4`, 35-45 seconds
   - Visual: go to Words, tap Calculate with Gemma, show progress/logs/backend.
   - Voiceover: "Gemma 4 E2B runs on device through LiteRT-LM. It extracts important words as structured JSON only. It does not advise, diagnose, or explain the user."

5. `05_borromean_words.mp4`, 35-45 seconds
   - Visual: show Borromean knot, object a fragments, personally important words, what I truly want, Gemma-extracted labels.
   - Voiceover: "The words are organized in a Borromean knot view: object a fragments, personally important words, and what I truly want. The user can edit, delete, export, or recalculate them."

6. `06_privacy_audio_export.mp4`, 25-30 seconds
   - Visual: show audio playback controls, Privacy tab, export options, model setup.
   - Voiceover: "The app is local-first. Audio playback can continue while the screen is locked. Notes, words, and audio exports remain under user control."

7. `07_closing.mp4`, 15-20 seconds
   - Visual: title card with the core sentence.
   - Voiceover: "The goal is not to make AI know the user. Most AI assistants speak for the user. My Words, My Way helps the user keep speaking until their own words become powerful."

## Build And Install

From the repository root:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

Install on a connected emulator or phone:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r .\app\build\outputs\apk\debug\app-debug.apk
```

Launch:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell monkey -p com.mywordsmyway 1
```

## Record A Clip

Use `record_clip.ps1`:

```powershell
.\demo\record_clip.ps1 -ClipName 03_record_note_flow -Seconds 35
```

The script records the Android screen and saves the clip to:

```text
demo/clips/03_record_note_flow.mp4
```

## Concatenate Clips

If `ffmpeg` is installed:

```powershell
.\demo\concat_clips.ps1
```

Output:

```text
demo/final/my_words_my_way_kaggle_demo.mp4
```

If `ffmpeg` is not installed, import the clips from `demo/clips` into Clipchamp, DaVinci Resolve, iMovie, Premiere, or another video editor in the order listed above.

