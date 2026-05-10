# Android Mockups

These are low-fidelity Compose mockups. The visual style should be quiet, minimal, and focused on the user's words.

## 1. Weekly Access

```text
My Words, My Way

1 free conversation this week

The app asks one question and preserves your own words.

[ Start free conversation ]

Small text:
After your weekly free conversation, a paid conversation asks you to pause
and take your words seriously. This app is not psychoanalysis.
```

After free conversation is used:

```text
My Words, My Way

This week's free conversation has been used.

Paid conversation

Paying is a boundary. It asks you to slow down and write carefully.
It does not make this app psychoanalysis.

[ Continue with paid conversation ]
[ Not now ]
```

## 2. Record

```text
What does that make you think about?



              [  Hold to record  ]



Memos saved: 2

[ Finish conversation ]
```

Safety support state:

```text
I am here listen to you.
This app is not the right support tool for immediate danger.
Please contact emergency services or a trusted person now.

What does that make you think about?

[ Record again ]
[ Finish conversation ]
```

## 3. Write Note

```text
What was this conversation about?

Write only what you want to keep.

[ text area ]

[ Save note and review words ]
```

Validation:

```text
Write a note before reviewing suggested words.
```

## 4. Review Suggestions

```text
Review possible words

The app found nouns in your conversation.
If you keep a word, it will connect to the note you wrote.

PHONE
This noun appeared in a current scene that you associated with earlier memories.
[ Keep ] [ Rename ] [ Delete ]

WINDOW
This noun appeared in a current scene or childhood/mother-related part of your conversation.
[ Keep ] [ Rename ] [ Delete ]

HAIR
This noun appeared near a remembered appearance detail.
[ Keep ] [ Rename ] [ Delete ]
```

Rename sheet:

```text
Rename word

Current: WINDOW

[ new word input ]

[ Save ] [ Cancel ]
```

## 5. My Words

```text
My Words

[ Search words and linked notes ]

WINDOW

Linked notes:
- I noticed the phone today led me back to the window in the old apartment...
- I remembered the room and the light outside...

[ Rename ] [ Merge ] [ Delete ]

PHONE

Linked notes:
- I noticed the phone today led me back to the window...

[ Rename ] [ Merge ] [ Delete ]
```

## 6. My Notes

```text
My Notes

[ Search notes ]
[ Start date ] [ End date ]

May 10, 2026
I noticed the phone today led me back to the window in the old apartment...

May 03, 2026
I remembered the kitchen and the warm room...

[ Export notes only ]
```

Export confirmation:

```text
Export notes only?

This export includes:
- your written notes
- dates
- linked confirmed nouns

It does not include:
- audio
- hidden transcripts
- transcript evidence spans

[ Export ] [ Cancel ]
```

## 7. Privacy And Storage

```text
Privacy and storage

Local app data
742 MB / 1 GB

Hidden audio and transcripts stay on this device.
When app data is larger than 1 GB, the oldest memo audio and transcript text are deleted.
Your written notes and saved words remain unless you delete them.

[ Delete old audio now ]
[ Delete all hidden transcripts ]
[ Export notes only ]
```

## Navigation

Bottom navigation:

```text
Record | Notes | Words | Privacy
```

Suggested Compose routes:

```text
access
record/{conversationId}
write-note/{conversationId}
review/{conversationId}
notes
words
privacy
```

