# My Words, My Way: A Local-First Listening App Where AI Learns to Stay Quiet

## Summary

My Words, My Way is a local-first reflective voice app built with Gemma 4. The app’s central design choice is restraint: during a session, AI does not advise, diagnose, summarize, or interpret. It only asks one question after each voice memo:

> What does that make you think about?

When the user stops, they write their own note. The raw voice memos and transcripts remain hidden locally. Gemma 4 then extracts candidate concrete nouns from the hidden transcript: objects, rooms, places, body parts, bodily materials, clothes, sounds, photos, doors, windows, beds, schools, kitchens, toys, food, letters, phones, keys, and remembered appearance or body details of the user's mother such as hair, eyes, smile, face, hands, breast, and voice. It can also extract the child's own early bodily-control nouns, such as feces/excrement, when the scene connects them to mother, play, offering, mother's smile, or what the child thought could make mother happy. Same-meaning words can be normalized to the same noun, so "picture" can become `PHOTO`, "luggage" can become `SUITCASE`, and "feces" can become `EXCREMENT`. These nouns are not only childhood words. They can come from what is happening to the user now, then show what that current event made the user think about through the question, "What does that make you think about?" The model preserves current scenes, early scenes of care, absence, mother, and what the child imagined the mother wanted. But the model never decides what the nouns mean. The user can keep, rename, delete, merge, or edit every suggestion, and each kept noun links back to the note or notes the user wrote for the related conversation.

The goal is not to create an AI therapist. The goal is to help the user build a private, searchable map of their own words.

## Problem

Most AI reflection products respond too quickly. They summarize the user, explain the user, and often replace the user’s own language with polished psychological language. That can be harmful in intimate reflection, because the model starts to sound like an authority.

This app takes the opposite approach. It is designed for people who want a private way to follow their own associations, preserve important notes, and slowly notice concrete nouns from their speech: nouns from current life, nouns connected to early scenes with mother, nouns connected to mother leaving or becoming unavailable, and nouns connected to what the child imagined mother wanted. The app preserves these words without interpreting the user.

## Creator Story

This project comes from my own experience with Lacanian psychoanalysis and artificial intelligence. I have more than five years of Lacanian psychoanalysis experience and five years of professional Lacanian psychoanalysis training. My own Lacanian psychoanalysis helped my words become stronger and helped me gradually live the life I wanted to live.

I understand Lacan's theory through language and through subjective topology. I can communicate with professional Lacanian psychoanalysts, and my training is centered on listening to each individual story as singular, no matter what that story is. For me, psychoanalysis is not a system that already knows the person. It is a practice of listening where the subject's own words can begin to work.

I am also a professional software developer with an engineering background in artificial intelligence. From that position, I see current LLMs as capable of something closer to elementary psychological counseling: they can summarize, respond, comfort, classify, and give generic guidance. But they cannot work as psychoanalysis. They do not have the position of an analyst, and they should not pretend to know the structure or truth of an individual subject.

Another reason is transference. In psychoanalysis, transference is one of the most important things that develops through sustained listening. In most situations, it can only develop between people: from one person respecting and listening to whatever words are said, from trying to understand another person without taking over their speech, and from offering a room where the person can say anything with the support to do what they want to do with their own life. This does not come from a model knowing everything. It comes from the visitor's own words.

That is why I built this app. Considering current model ability, the app does not ask AI to become a psychoanalyst. It asks AI to do something narrower: listen quietly, preserve words, extract concrete nouns, and help the user keep speaking until their own words become powerful.

Honestly, the current app still cannot act as a psychoanalyst. When AI cannot help people mentally, people should be able to use their money to find a human psychoanalyst. My hope is to make a small part of Lacanian psychoanalytic practice more accessible to individuals, so that people can begin from their own words and, in the future, decide whether finding their own Lacanian psychoanalysis would be helpful for their life.

## Why Gemma 4

Gemma 4 is central to the product for four reasons:

1. **Local-first privacy.** The app is built for intimate speech and should not require sending raw voice memos to a cloud model.
2. **Audio understanding.** Gemma 4 E2B/E4B support native audio input, making short voice-memo processing practical.
3. **Long context.** Multi-round reflection sessions can be processed as one structured session.
4. **Structured output and function calling.** Gemma can produce JSON candidate nouns and safety flags that update a local SQLite database.

For the demo, the recommended model is Gemma 4 E4B. E2B can be used for lighter devices. A larger Gemma 4 model can optionally be used for offline batch re-analysis, but the user-facing experience should remain local and private.

## User Experience

The app has five screens:

1. **Record**  
   The user records a memo. The app only asks: “What does that make you think about?”

2. **Write Note**  
   After finishing, the user writes what they believe the conversation was about. This note is visible and searchable.

3. **My Notes**  
   The user sees their own notes, not raw transcripts.

4. **My Words**  
   The user sees confirmed nouns. Each noun links to one or more user-written notes from related conversations.

5. **Review Suggestions**  
   Gemma suggests nouns after each session. The user can keep, rename, delete, merge, or edit them.

6. **Search and Extract Notes**  
   The user can search visible memory by text or date and extract only the notes they wrote for each multi-memo conversation. The hidden transcript remains saved locally, but it is not exported to the user-facing extract.

7. **Weekly Free Conversation and Paid Reflection**  
   Each user gets one free conversation per week. After that, the app asks for a paid conversation. This is not because the app is psychoanalysis; it is a design boundary that asks the user to pause, value the session, and write careful notes about their own words.

## What Gemma Extracts

Gemma does not extract themes like “abandonment,” “trauma,” “approval,” or “desire.” Those are too interpretive. It extracts concrete nouns that can hold a scene:

- window
- door
- kitchen
- bed
- school
- dress
- hair
- eyes
- smile
- face
- hand
- breast
- voice
- excrement
- light
- room
- phone
- photo
- suitcase
- key
- home

In this vocabulary, `EXCREMENT` does not mean the mother's excrement. It means the child's own feces/excrement in an early bodily-control scene: for example, a remembered scene where the child could produce, hold, release, offer, or play with something, and the mother smiled or seemed happy. The app still does not say what this means; it only preserves the noun and lets the user decide whether it matters.

Example: a user says, “When I was little, I sat near the window waiting for my mom to come home.” Gemma may suggest `WINDOW`, `ROOM`, and `LIGHT`. The user may keep `WINDOW`, delete `LIGHT`, and connect `WINDOW` to the note they wrote for that conversation.

## Architecture

Frontend:

- Static HTML/JavaScript prototype for the demo
- Record flow
- note entry
- review suggestions
- word management
- local search
- text/date search and notes-only extraction

Backend:

- FastAPI
- SQLite
- local audio storage
- hidden transcript table
- audio deletion that preserves hidden transcripts
- weekly free conversation and paid-conversation acknowledgement
- automatic cleanup when local app data exceeds 1 GB
- suggestion table
- noun table
- search over visible notes and confirmed nouns

Model layer:

- Gemma 4 E4B/E2B for audio transcription, safety classification, and noun extraction
- mock mode for demo reproducibility
- optional OpenAI-compatible local endpoint for vLLM/llama.cpp/Ollama-style serving

Data design:

- visible: notes, confirmed nouns, note links
- hidden: audio paths, transcripts, evidence spans
- removable: stored voice memo audio can be deleted while transcripts remain saved locally
- size-limited: oldest memo audio and transcript text are deleted when local app data exceeds 1 GB
- access: one free conversation per week, then paid conversation acknowledgement
- controlled: every model suggestion must be accepted or edited by the user

## Safety and Trust

The app is explicitly not therapy. It does not diagnose, treat, or give medical advice. It avoids interpretation, avoids claims about the user, and never says “this word means X.” Instead, it says:

> The app found this noun in a current scene or childhood/mother-related part of your conversation. Do you want to keep, rename, or delete it?

If self-harm or immediate danger appears, the app still works: it keeps listening, asks the same question, saves the user's words locally, and can still extract nouns later. It also says, "I am here listen to you," shows crisis resources, and encourages the user to contact emergency services, a trusted person, or a qualified professional.

## Challenges

The hardest design problem was that psychoanalysis is exactly the kind of work even the best LLM should not pretend to do. A professional psychoanalyst may have years of training and theory, but when a new visitor arrives, the analyst still begins from not-knowing. Theory can orient listening, but it can never cover every individual in the world.

This is very different from the usual dream of large language models: a machine that seems to know everything. Psychoanalytic work is almost the opposite. The analyst is not the one who already knows the person. Even Lacan, late in his work, turned toward topology and drew many different structures because clinical visitors could not be reduced to one universal explanation.

So the challenge was not making Gemma smart enough to interpret the user. The challenge was designing an app where Gemma stays far away from the position of the one-who-knows. We solved this by separating the system into three layers:

1. Gemma may detect candidate concrete nouns.
2. The database may link nouns to hidden evidence and user-written notes.
3. Only the user can decide which nouns belong with which written notes.

The second challenge was privacy. Search can use hidden transcripts internally, but the frontend only shows user-written notes and confirmed words. This gives the user useful memory without exposing raw speech.

The third challenge was safety. The app’s normal loop is intentionally minimal, and crisis mode adds support language and emergency-resource guidance without taking the user's words away.

The fourth challenge was authorship. The app requires a user-written note before Gemma noun extraction, because the most important record of the conversation should be what the user can understand at that moment. Gemma's extracted words can then help the user notice how current life can lead into memories of early time with their mother without replacing the user's own note.

The fifth challenge was making reflection feel intentional. The prototype gives each user one free conversation per week; after that, paid conversations create a small friction that can help the user take their words seriously. This is not a claim that the app is psychoanalysis.

## Future Extensibility

The prototype is intentionally narrow, but the architecture leaves room for a documentation-only Lacanian extension. A later version could add an optional transference layer that stores user- or clinician-edited transference-type tags such as authority or subject-supposed-to-know, demand for recognition, refusal or avoidance, certainty without question, and boundary testing.

Those tags could support research views that distinguish Lacanian clinical or psychological structures such as neurosis, psychosis, and perversion. If future models become capable enough and the feature is human-reviewed, this could help the app choose different listening strategies for different users. This should not become an automatic diagnosis feature. Gemma should only organize evidence and user-approved notes; any structural reading must remain optional, editable, and interpreted by a human.

## Impact

My Words, My Way demonstrates a different pattern for AI for good: not more persuasion, more interpretation, or more advice, but a careful tool that helps people preserve their own words. It is useful for digital wellbeing because it gives users a private, searchable self-reflection system where the model is helpful without becoming the authority.

The project’s core principle is simple:

> Most AI assistants speak for the user. My Words, My Way helps the user speak until their own words become powerful.
