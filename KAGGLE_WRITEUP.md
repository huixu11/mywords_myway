# My Words, My Way: A Local-First Listening App Where Gemma Learns to Stay Quiet

**Subtitle:** A private Android voice-reflection app that uses Gemma 4 E2B on device to preserve important words without interpreting the user.

**Primary track:** Safety & Trust  
**Secondary themes:** Digital wellbeing, digital equity, local-first AI

## Public Artifacts

- Introduction video: <https://youtu.be/ri4lWGU7YTw>
- Public code repository: <https://github.com/huixu11/mywords_myway>
- Demo video: <https://youtu.be/9dnSckxvcxA>
- Live demo APK, tested on Pixel 6a: <https://github.com/huixu11/mywords_myway/releases/tag/v0.1-debug>

## Summary

My Words, My Way is a native Android app for private self-reflection. Its main design choice is restraint. Most AI assistants try to help by speaking more: they summarize, advise, interpret, comfort, and classify. In emotionally sensitive reflection, that can make the model feel like an authority over the user's life.

This app asks for a different kind of intelligence. The user records voice memos, writes their own notes, and can replay or export their local audio. Gemma 4 E2B runs on device through LiteRT-LM and extracts candidate important words from the user's note and memo text. The model does not decide what those words mean. It returns structured JSON with evidence spans, confidence, and a suggested category. The user can keep, edit, delete, recalculate, or export the result.

The core sentence of the project is: **the model does not decide meaning; it only preserves possible words, and the user decides what becomes true.**

According to Lacan's theory our own language has the power. In this Agentic AI world, agentic AI can solving everything, knowing everything. But to understand human minds, what we need is the opposite --- we don't know. Only every individual know about themselves. Most of the important words comes from our mom. It's because we love our mom. So I build the My words, My way to empower users with their own languages.

## Problem

The places that most need careful AI are often the places where privacy, trust, and limited connectivity matter most: a person reflecting at night, a community with limited internet, or anyone who does not want their private voice sent to a server. Reflection tools also face a subtler safety problem. If an AI responds too quickly with explanations, it can replace the user's language with the model's language.

This project comes from my experience with Lacanian psychoanalysis, psychoanalytic training, and engineering. Human psychoanalysis depends on listening, silence, and transference. Current LLMs can support reflection, but they should not pretend to be psychoanalysts. They should not diagnose a person, claim to know their truth, or turn private speech into polished psychological conclusions.

My Words, My Way solves that problem by making restraint the feature. The app gives the user a private place to speak and write, then uses Gemma 4 only to preserve possible words that the user may want to keep.

## Product Experience

The Android app has three main areas.

**Notes** is the user's visible memory. Users can create notes, attach voice memos, replay audio with persistent playback, delete audio, and export their material through Android sharing. The note is always written by the user.

**Words** shows a Lacanian-inspired Borromean knot view. Gemma-extracted words are grouped into "Object a fragments," "Personally important words," and "What I truly want." These categories are not diagnoses. They are editable containers for words that may matter to the user.

**Privacy** contains model setup, storage controls, and exports. The user can download or import the Gemma 4 E2B `.litertlm` model, see whether it is loadable, review calculation logs, and keep data local. There is no paywall for notes or voice reflections.

## How Gemma 4 Is Used

Gemma 4 E2B is used for local structured extraction, not open-ended advice. The app targets phone-class hardware, so the E2B variant is a practical choice for an Android demo. The model file is `gemma-4-E2B-it.litertlm`, downloaded or imported by the user and stored in the app's external files directory.

The prompt explicitly tells Gemma:

> You are not a therapist, not a psychoanalyst, and not an advisor.

It also instructs the model not to interpret, diagnose, or tell the user what to do. The required output is JSON only:

```json
{
  "candidate_nouns": [
    {
      "Important word": "...",
      "type": "Object a fragments | Personally important words | What I truly want",
      "scene_type": "with_mom | mom_left | what_mom_wanted | unclear",
      "evidence_span": "...",
      "confidence": "low | medium | high",
      "suggestion": "add | update_existing | ignore"
    }
  ]
}
```

The app parses that JSON, stores extracted words locally, marks processed notes, and lets edited notes be recalculated. If the Gemma model is not configured, the app keeps working for notes and voice memos while the Words calculation explains what is missing.

## Technical Architecture

The project is a Kotlin Android app using Jetpack Compose, Room, Coroutines, DataStore, WorkManager, and LiteRT-LM.

- Room stores folders, notes, voice memo metadata, Borromean knots, extracted words, and processing status.
- Audio files stay on device and can be exported by the user.
- DataStore stores the Gemma model path and persistent UI/calculation state.
- LiteRT-LM loads Gemma 4 E2B from the local `.litertlm` path and runs generation on CPU.
- The model layer limits prompt size, uses a conservative CPU profile, and releases the LiteRT-LM engine and conversation after each generation to protect phone memory.
- A foreground audio service allows voice memos to keep playing when the screen is locked.
- Calculation progress, backend status, errors, and logs are visible so on-device inference does not feel like a frozen screen.

The public repository is now focused on the Android implementation and submission assets, making the code path easy for judges to inspect.

## Safety and Trust

Safety is built into the product boundary. The app is not therapy, does not diagnose, does not treat mental-health conditions, and does not give advice. Gemma is used as a local extraction tool with a narrow JSON contract.

Privacy is also part of trust. Reflective speech and notes stay on the device. The user controls exports. The app can be experienced through the APK without login, cloud storage, or a paywall. When model setup is needed, the UI explains the state instead of silently failing.

Most importantly, every model output remains secondary. The user can edit or delete extracted words, and the user's own note remains the primary record.

## Challenges Overcome

The first challenge was designing a useful AI experience without making the AI sound like it knows the user. The solution was to restrict Gemma to evidence-based word extraction and leave meaning outside the model.

The second challenge was mobile performance. On-device inference is memory-sensitive, especially on Pixel 6a-class hardware. The app limits source length, processes notes incrementally, persists progress, and releases LiteRT-LM resources after generation.

The third challenge was transparency. Local inference can take time, so the app shows progress, logs, backend information, and user-readable errors. This makes the system inspectable instead of mysterious.

The fourth challenge was authorship. A user can edit notes, recalculate only changed material, and delete all Words data. The app treats Gemma output as a draft, never as truth.

## Real-World Utility and Impact

My Words, My Way demonstrates a safer pattern for personal AI: local intelligence that helps a person keep their own words private, searchable, and editable. It is useful for people who want a quiet reflection space, people who cannot rely on constant internet access, and people who are uncomfortable sending intimate speech to cloud services.

The "wow" factor is not that the AI says something brilliant. It is that Gemma 4 is powerful enough to help while staying quiet. In a world of agentic AI that tries to answer everything, this app shows another path: AI for good can also mean AI that knows when not to speak.

The core principle is:

> Most AI assistants speak for the user. My Words, My Way helps the user keep speaking until their  own words become powerful.
