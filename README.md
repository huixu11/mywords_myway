# My Words, My Way

**A local-first reflective voice app built with Gemma 4.**

My Words, My Way is a listening and memory app for private self-reflection. During a session, the app does not advise, diagnose, summarize, or interpret. It only asks one question:

> What does that make you think about?

After the user finishes speaking, they write their own note. Hidden audio/transcripts stay local. Gemma 4 then extracts candidate **concrete nouns** from the hidden transcript: objects, rooms, places, body parts, bodily materials, foods, clothes, sounds, doors, windows, beds, schools, photos, toys, phones, keys, and remembered appearance or body details of the user's mother such as hair, eyes, smile, face, hands, breast, and voice. It can also extract the child's own early bodily-control nouns, such as feces/excrement, when the scene connects them to mother, play, offering, mother's smile, or what the child thought could make mother happy. Same-meaning words can be normalized to the same noun, so "picture" can become `PHOTO`, "luggage" can become `SUITCASE`, and "feces" can become `EXCREMENT`. These nouns are not only childhood words. They can come from what is happening to the user now, then show what that current event made the user think about through the question, "What does that make you think about?" The model preserves current scenes, childhood scenes, and the user's association between them, but it never decides what they mean. The user can keep, rename, delete, merge, or edit every word, and each kept noun links back to the note or notes the user wrote for the related conversation.

## Why this matters

Many personal-reflection AI tools try to interpret the user. This app is designed around AI restraint: Gemma helps preserve and organize possible nouns, while the user remains the author of meaning.

**Core sentence:** The model does not decide meaning. It only preserves possible nouns from the user’s speech. The user decides what becomes true.

## Creator Note

This app comes from my own Lacanian psychoanalysis experience, professional Lacanian psychoanalysis training, and engineering background in artificial intelligence. My own analysis helped my words become stronger and helped me gradually live the life I wanted to live. Current LLMs can support reflection, but they cannot act as psychoanalysts. Transference develops through sustained human listening, respect, and a room where someone can say anything. This app is built from that limit: it uses AI to preserve words and notes, not to know the user's truth.

## Hackathon positioning

- Track fit: Safety & Trust / Digital Wellbeing / Local-first AI for personal reflection
- Main value: private, local, user-controlled reflection
- Technical value: Gemma 4 E4B/E2B audio + structured JSON + safety routing
- UX value: one-question conversation, visible notes, editable noun map

## Demo flow

1. User records a short voice memo.
2. App asks: “What does that make you think about?”
3. User records again.
4. User stops and writes: “What was this conversation about?”
5. Gemma 4 processes hidden transcript/audio.
6. App suggests candidate nouns.
7. User reviews suggestions and controls what is saved.
8. Search only shows user notes and confirmed words, not raw transcripts.

## Memory Controls

- Users can delete stored voice memo audio while keeping the hidden transcript saved locally.
- Users can search visible memory by note text, confirmed word, linked note, start date, or end date.
- Users can extract only the notes they wrote for each multi-memo conversation. Raw transcripts and audio are not included in the extract.
- The app requires a user-written note before Gemma noun suggestions are generated, so each session ends with the user's own most important understanding at that moment.
- Gemma 4 extracted words are meant to help the user notice how current life can lead into memories of early time with their mother, not to explain or diagnose the user.

## Payment and Storage Boundaries

- Each local user gets one free conversation per week.
- After the free weekly conversation, the prototype requires a paid-conversation acknowledgement before starting another session. A production build would replace this acknowledgement with a real payment provider.
- The payment is part of the product's reflection design: paying a small amount asks the user to pause, take the conversation seriously, and write careful notes about what they understood.
- When local app data grows beyond 1 GB, the app deletes the oldest voice memo audio and hidden transcript text until storage is under the limit.
- User-written notes, confirmed nouns, and note links are kept visible unless the user deletes them.

## Prototype status

This package contains a runnable local prototype skeleton:

- FastAPI backend
- SQLite schema
- static HTML demo UI
- mock Gemma mode for demo without model setup
- optional OpenAI-compatible endpoint mode for local Gemma served by vLLM/llama.cpp/Ollama-style adapters
- Kaggle writeup draft
- 3-minute demo video script
- prompt pack
- schema and safety policy

## Run locally

```bash
cd my_words_my_way_submission
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
python backend/app.py
```

Then open:

```text
http://localhost:8000
```

## Environment variables

```bash
GEMMA_BACKEND=mock
# Optional if you expose Gemma 4 through an OpenAI-compatible local server:
GEMMA_BACKEND=openai_compatible
GEMMA_BASE_URL=http://localhost:8001/v1
GEMMA_API_KEY=local
GEMMA_MODEL=google/gemma-4-E4B-it
```

## Extensibility

The current prototype stops at noun preservation. A future extension can add a documented transference layer without changing the app's safety boundary. In that mode, the app could store optional, user- or clinician-edited transference-type tags such as authority or subject-supposed-to-know, demand for recognition, refusal or avoidance, certainty without question, and boundary testing.

Those tags could support Lacanian research views that distinguish clinical or psychological structures such as neurosis, psychosis, and perversion. If future models become capable enough and the feature is human-reviewed, this could help the app choose different listening strategies for different users. This extension must remain descriptive and opt-in. Gemma must not diagnose a structure, tell the user what they are, or treat Lacanian categories as medical facts; it can only organize transcript evidence and user-approved notes for later human interpretation.

## Important safety note

This app is **not therapy**. It does not diagnose, treat, or give mental-health advice. If the safety detector sees self-harm or immediate danger, the app still works: it keeps listening, asks the same question, saves the user's words locally, and can still extract nouns later. It also shows "I am here listen to you" and encourages the user to contact trusted people or emergency services.
