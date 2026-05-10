# 3-Minute Demo Video Script

## 0:00-0:20 - Hook

Most AI assistants try to answer us immediately.

But for intimate reflection, that can be the wrong move. Sometimes the most helpful AI is the one that stays quiet.

This is **My Words, My Way**, a local-first listening app built with Gemma 4.

## 0:20-0:45 - Problem

When people use AI for self-reflection, the model often summarizes, interprets, and explains them. It can sound confident, even when it should not be the authority.

Our app is built around one rule:

The model does not decide meaning. The user does.

## 0:45-1:20 - Demo: Recording

Here is the full conversation interface.

The user records a voice memo.

The app asks only one question:

"What does that make you think about?"

The user records again.

No advice. No diagnosis. No interpretation.

The user stops when they want to stop.

## 1:20-1:45 - Demo: User Note

After the session, the user writes their own note:

"I noticed the phone today led me back to the window in the old apartment. I think I was waiting for her to come back."

This note is visible forever unless the user deletes it. The raw conversation stays hidden.

## 1:45-2:20 - Demo: Gemma 4 Suggestions

Now Gemma 4 processes the hidden transcript locally.

It extracts concrete nouns, not abstract psychological labels.
Those nouns can come from the current scene, like PHONE, and from the childhood scene it made the user think about.
Those nouns can include remembered appearance details, like hair, eyes, or smile, when they appear in a childhood/mother-related scene.
They can also include early bodily-control nouns like EXCREMENT when the user remembers their own feces as playful, controllable, or connected to the mother's smile.

It suggests:

- PHONE
- WINDOW
- HAIR
- SMILE
- ROOM
- LIGHT

The app does not say what these words mean. It only says:

"These nouns appeared in a current scene or childhood/mother-related part of your conversation. Do you want to keep, rename, or delete them?"

The user keeps WINDOW, deletes LIGHT, and connects WINDOW to the note she wrote for that conversation.

## 2:20-2:40 - Demo: Search and Memory

Later, the user searches "love."

The app does not show raw transcripts.

It shows only visible notes and confirmed words:

- Note: "I noticed the phone today..."
- Word: WINDOW

The user slowly builds a private map of her own words.

## 2:40-3:00 - Technical + Closing

Gemma 4 is used for local audio understanding, safety detection, and structured JSON noun extraction. The app uses FastAPI, SQLite, local audio storage, and a user-controlled review queue.

This is not an AI therapist. It is a quiet tool for reflective independence.

Most AI assistants speak for the user. My Words, My Way helps the user speak until their own words become powerful.
