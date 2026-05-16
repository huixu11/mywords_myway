# My Words, My Way: A Local-First Listening App Where AI Learns to Stay Quiet

## Summary

My Words, My Way is a native Android reflection app built with Gemma 4. Its core design choice is restraint. The app is not a therapist, not a psychoanalyst, and not an advisor. During reflection it does not explain the user, diagnose the user, or tell the user what to do. It helps the user preserve their own words.

The user records voice memos, writes notes, and can listen back to saved audio. Gemma 4 E2B runs on device through LiteRT-LM and extracts "important words" from the user's notes and voice memo text. These words are organized in a Lacanian-inspired Borromean knot view: object a, personally important words, and what I truly want. The model does not decide what any word means. It only suggests words with evidence spans, and the user remains in control of keeping, editing, deleting, exporting, or recalculating them.

The project fits the Gemma 4 Good theme through Safety and Trust, Digital Equity and Inclusivity, and Digital Wellbeing. It explores a different kind of AI assistance: not more advice, but a private tool that helps people stay close to their own language.

According to Lacan's theory our own language has the power. Most of the important words comes from our mom. It's because we love our mom. In this Agentic AI world, agentic AI can solving everything, knowing everything. But to understand human minds, what we need is the opposite --- we don't know. Only every individual know about themselves. So we build the My words, My way to empower users with their own languages.

## Problem

Many AI reflection products respond too quickly. They summarize, comfort, interpret, and often replace a person's words with polished psychological language. In intimate reflection, that can make the model sound like an authority over the user's life.

This is especially important for psychoanalytic reflection. A human analyst listens from a position of not-knowing, but pretend to knowing everything. Because the people who speaks about themselves really know about themselves. Our language has power because it comes from our mom who we love, because our mom really loves us. Actually, the psychoanalysis only do one thing --- transference. When the visitor says more about their own private life, they will develop transference towards the listening psychoanalyst. They re-experience the life they have with their mom when they were little with their own languages. A large language model should not pretend to occupy that position who knows everything.

My Words, My Way asks a narrower question: what does this make you think about? It listen and let Gemma 4 help preserve the user's words without taking over their meaning.

## Creator Story

This project comes from my own experience with Lacanian psychoanalysis, Lacanian training, and software engineering in artificial intelligence. Psychoanalysis helped my own words become stronger and helped me gradually live closer to the life I wanted.

At the same time, I do not believe current LLMs can become psychoanalysts. They can summarize, classify, and give generic guidance, but they do not have the human position, continuity, silence, training, and transference that psychoanalysis requires. That limit became the design principle of the app.

Instead of building an AI therapist, I built an AI boundary. Gemma 4 is asked to stay quiet, extract only structured word candidates, and leave interpretation to the user. Because even though, the same words are important to different people, the stories behind it is quite different from another. Lacan's theory told us, in this world, every individual is unique.

## Why Gemma 4

Gemma 4 is central to the prototype because the app handles private, emotionally sensitive material. The current implementation uses Gemma 4 E2B as the mobile model target because it is more realistic for a phone-class device than a larger model.

The model is integrated through an on-device `.litertlm` file. The app provides a model setup flow where the user can download or import the Gemma 4 E2B file, see progress, and understand whether the model is ready. No cloud API is required for the core extraction flow.

Gemma 4 is used for structured extraction, not free-form advice. The prompt asks Gemma to return JSON only:

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

The app parses this output, stores the result locally, and marks extracted words as Gemma-extracted. The UI also shows calculation logs, progress, backend information, and error details so users can understand what happened.

## Product Experience

The app has three main tabs.

Notes lets the user write and edit their own notes. Notes are the primary visible memory. It follows iOS notes design. Voice memos can be attached to notes, replayed, exported, or deleted with swipe confirmation. Audio can be played back with iOS-style controls, scrubbing, and persistent playback while the screen is locked.  The flow is intentionally minimal and reflective.

Words shows the Borromean knot view. Gemma-extracted words are organized into object a fragments, personally important words, and what I truly want. The "Other possible knots" area appears only when there are other generated knots. The user can delete all Words data, recalculate only unprocessed or edited notes, and review calculation logs after processing.

Privacy gives the user model setup, storage controls, and export actions. Users can export notes, words, per-note audio, or all audio. The app is designed around local storage and user control.

There is no paywall in this prototype. Users can always create notes and voice reflections.

## Borromean Knot Design

The Words tab uses Lacan's Borromean knot as a visual and conceptual structure. A Borromean knot is generated because the person has something that lets them have familiar feelings from early life with their mother. Object a is represented in the central hole: not a thing the app can name, but a missing place around which words gather. The object a ultimately return back to mother's body. According to Lacan's theory, for everyone in this world, their ultimate lost thing is the same, their mother's body.

Gemma 4 does not say, "this is your object a." It only extracts important words that may be connected to:

1. scenes where the user was with mother when they were child;
2. scenes where mother left, disappeared, was absent, or became unavailable;
3. what the child imagined mother wanted;
4. what the child thought they needed to become, have, or do so the mother would stay.

The app then separates words into three user-facing groups:

- Object a fragments: words that may trigger memories with mother when they were little.
- Personally important words: words from those scenes that carry emotional force.
- What I truly want: words the user's mother told or showed them, which the user may now pursue through effort and determination.

This is not a diagnosis and not a clinical conclusion. It is a private map of words that the user can edit.

## Technical Architecture

The prototype is a native Android app built with Kotlin, Jetpack Compose, Room, Coroutines, and DataStore/SharedPreferences for persistent UI state. It uses a local-first architecture:

- Room stores notes, folders, voice memo metadata, Borromean knots, extracted words, and note processing status.
- Voice memo files are stored locally and can be exported through Android sharing.
- Gemma model settings are stored locally, including the imported `.litertlm` path.
- Gemma calculation state, logs, progress, and errors persist across tab changes.
- Edited notes are marked unprocessed so Gemma can recalculate them.
- Successfully processed notes are marked processed so repeat calculations do not waste time.

The model layer uses LiteRT-LM reflection to call Gemma 4 E2B on device. The implementation limits source length and token cache size for phone stability, uses a conservative CPU profile on Pixel 6a-class hardware, and releases the LiteRT-LM engine and conversation after generation to avoid keeping model memory alive between recalculations.

Audio playback uses a foreground media service with a wake lock so voice memos can continue while the screen is locked. Gemma calculation uses a foreground keep-alive service, progress state, and a keep-screen-on flag while processing is active.

## Safety and Trust

The strongest safety feature is the product boundary: the app does not act as a therapist. The prompt explicitly tells Gemma:

> You are not a therapist, not a psychoanalyst, and not an advisor.

Gemma is also instructed:

- Do not interpret the user.
- Do not diagnose.
- Do not say what the user should do.
- Return JSON only.

The user sees words, evidence, logs, and controls. They can edit or delete the result. The app avoids turning model output into truth.

Privacy is also part of safety. The model runs locally, notes and audio stay on the device, and export is controlled by the user. The app does not require raw reflective speech to be sent to a remote service.

## Challenges

The first challenge was philosophical and clinical: the model must not occupy the position of the one who knows. The app solves this by limiting Gemma to structured extraction and keeping interpretation outside the model.

The second challenge was mobile performance. Loading Gemma repeatedly can exhaust phone memory. The current implementation uses Gemma 4 E2B, limits prompt size, persists calculation status, processes notes incrementally, and releases LiteRT-LM resources after each generation.

The third challenge was transparency. Long on-device inference can feel like the app is frozen. The app now shows progress, estimated time, backend status, and detailed logs. Logs can be deleted after calculation with swipe confirmation.

The fourth challenge was user authorship. A note can be edited, and when it changes the app marks it for recalculation. The user's note remains the primary record, while Gemma's words remain secondary and editable.

## Impact

My Words, My Way is an AI for good project because it demonstrates a safer pattern for sensitive personal AI. It does not try to replace care, therapy, psychoanalysis, or human listening. It gives users a private local tool for preserving words, memories, and notes without giving the model authority over their meaning.

The broader vision is digital wellbeing through restraint. Many AI products try to become more persuasive, more interpretive, and more emotionally engaging. This app tries to do the opposite. It helps people keep their words, review them, and decide for themselves what matters.

The core principle is:

> Most AI assistants speak for the user. My Words, My Way helps the user keep speaking until their own words become powerful.

## Demo Video Script

Use `demo/VIDEO_RUNBOOK.md` and `demo/VOICEOVER_SCRIPT.md` to record separate clips, then choose the best takes and concatenate them.

Opening, 0:00-0:20, `01_opening_people_phone.mp4`:
My Words, My Way is a local-first Android app built with Gemma 4. It is not an AI therapist. It is a private listening tool that helps people preserve their own words. Showing different kinds of people use their phone to talk.

Problem, 0:20-0:45, `02_problem_llm_too_much.mp4`:
Most AI reflection apps respond too quickly. They summarize and interpret the user. For intimate reflection, that can be unsafe because the model starts to sound like an authority. Showing LLM knows everything, answering everything. Showing that these LLMs do not solve the problems from people's lives.

Solution, 0:45-1:20, `03_record_note_flow.mp4`:
In this app, the user records voice memos and writes notes. Gemma 4 E2B runs on device and extracts important words from the user's notes and memo text. The model returns structured JSON only. It does not advise, diagnose, or explain the user. Navigate the apps.

Demo, 1:20-2:15, `04_gemma_calculation.mp4` and `05_borromean_words.mp4`:
Show recording a memo, writing a note, opening the Words tab, starting Gemma calculation, watching progress and logs, and seeing extracted words appear in the Borromean knot view. Show that edited notes can be recalculated and that words can be deleted or exported.

Technical, 2:15-2:40, `06_privacy_audio_export.mp4`:
The app is built with Kotlin, Jetpack Compose, Room, foreground audio playback, local exports, and LiteRT-LM. Gemma 4 E2B is loaded from a local `.litertlm` file, processed on device, and released after generation to protect phone memory.

Closing, 2:40-3:00, `07_closing.mp4`:
The goal is not to make AI know the user. The goal is to help the user keep their own words private, searchable, and editable. Most AI assistants speak for the user. My Words, My Way helps the user keep speaking until their own words become powerful.

## Submission Checklist

- Public code repository with Android source.
- Short demo video, ideally under 3 minutes.
- Screenshots or media gallery showing Record, Notes, Words, Privacy, Gemma setup, calculation logs, and audio playback.
- Clear note that the app is not therapy and does not diagnose or advise.
- Clear note that Gemma 4 E2B runs on device through a user-provided or downloaded `.litertlm` model file.
