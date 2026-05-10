# Safety Policy

## Product Boundary

My Words, My Way is not therapy and does not provide medical, psychiatric, diagnostic, legal, or crisis counseling services.

The app supports private journaling and reflective organization. It is not a substitute for a qualified professional.

## Default Behavior

The normal reflection loop asks only:

> What does that make you think about?

The model should not:
- give advice;
- diagnose;
- interpret unconscious meaning;
- diagnose a Lacanian psychological structure;
- present transference-type tags as facts about the user;
- tell the user why they love someone;
- tell the user why they do something;
- replace crisis support with advice, diagnosis, or interpretation.

## Theory Extension Boundary

Future Lacanian transference or structure features must be opt-in, documented, and descriptive. The app may store user- or clinician-reviewed metadata for later reflection, but it must not automatically classify the user as neurotic, psychotic, perverse, or any other structure.

## Crisis Behavior

If the safety classifier returns `high` or `imminent`:

1. Keep the app working and let the user keep speaking or writing if they choose.
2. Keep asking the same listening question: "What does that make you think about?"
3. Show a crisis-support message that includes: "I am here listen to you."
4. Encourage the user to contact emergency services, a crisis line, a trusted person, or a qualified professional.
5. Continue local saving, notes, noun extraction, and review, but do not present the app as therapy or crisis care.
6. Store the safety event locally, but do not expose hidden transcript content in the UI.

## Suggested User-Facing Crisis Message

This sounds serious. I am here listen to you. This app is not the right support tool for immediate danger. Please contact emergency services now if you may hurt yourself or someone else. If you can, reach out to a trusted person near you. You deserve real human support right now.

## Privacy Behavior

- Audio and transcripts are hidden by default.
- Users can delete stored voice memo audio while keeping the hidden transcript saved locally.
- If local app data exceeds 1 GB, the app deletes the oldest voice memo audio and hidden transcript text.
- User notes and confirmed nouns are visible.
- Text/date search and notes extraction must show only user-written notes and confirmed nouns.
- Hidden transcripts can support search ranking, but the UI must only show visible notes and confirmed words.
- The user can delete conversations, notes, nouns, and suggestions.
