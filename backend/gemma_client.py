import json
import os
import re
from typing import Any, Dict, List

import requests

GEMMA_BACKEND = os.getenv("GEMMA_BACKEND", "mock")
GEMMA_BASE_URL = os.getenv("GEMMA_BASE_URL", "http://localhost:8001/v1")
GEMMA_API_KEY = os.getenv("GEMMA_API_KEY", "local")
GEMMA_MODEL = os.getenv("GEMMA_MODEL", "google/gemma-4-E4B-it")

CONCRETE_NOUNS = [
    "phone", "window", "door", "kitchen", "bed", "school", "dress", "hair", "eyes", "smile",
    "face", "mouth", "hand", "breast", "voice",
    "room", "light", "milk", "table", "photo", "toy", "suitcase", "key",
    "home", "car", "rain", "station", "hospital", "letter", "mirror", "bag", "excrement"
]

NOUN_ALIASES = {
    "breast": ["chest", "bosom", "boob", "nipple"],
    "excrement": [
        "feces", "faeces", "poop", "poo", "pooped", "stool", "bowel movement",
        "defecation", "defecated", "shit"
    ],
    "eyes": ["eye", "gaze"],
    "smile": ["grin"],
    "mouth": ["lips", "lip"],
    "hand": ["hands", "palm", "finger"],
    "photo": ["picture", "photograph"],
    "phone": ["telephone"],
    "suitcase": ["luggage"],
    "bag": ["purse"],
    "home": ["house"],
    "car": ["automobile"],
}

APPEARANCE_NOUNS = {"hair", "eyes", "smile", "face", "mouth", "hand", "breast", "voice"}
CHILD_OFFER_NOUNS = {"excrement"}
CURRENT_TRIGGER_TERMS = {
    "today", "now", "currently", "this morning", "tonight", "yesterday", "work",
    "office", "meeting", "boss", "friend", "partner", "boyfriend", "girlfriend",
    "husband", "wife", "child", "children", "text", "message", "email", "call",
    "apartment"
}

ABSTRACT_BLOCKLIST = {
    "love", "trauma", "abandonment", "anxiety", "success", "approval",
    "independence", "desire", "career", "achievement", "sadness", "fear"
}

NOUN_EXTRACTION_PROMPT = """
You are not a therapist, not a psychoanalyst, and not an advisor.

From the transcript, extract only concrete nouns from the user's association chain.
These nouns may come from current life, from early experience with mother, or from the
link between a current scene and an early scene. The prompt "What does that make you
think about?" is how current events can lead to childhood/mother associations.

Focus on:
1. nouns from a current-time scene that led the user to remember or associate;
2. nouns connected to scenes where the child was with mother;
3. nouns connected to scenes where mother left, disappeared, was absent, or became unavailable;
4. nouns connected to what the child imagined mother wanted;
5. nouns connected to what the child thought they needed to become, have, or do so mother would stay.
6. nouns connected to mother's remembered appearance or body, including hair, eyes, smile,
   face, hands, breast, voice, clothes, scent, or other concrete sensory details.
7. nouns connected to the child's own bodily production or control, including feces/excrement,
   when the scene links it to mother, play, offering, a gift, praise, mother's smile,
   or what the child thought could make mother happy.

Extract nouns only.
If the transcript uses a same-meaning word, return the closest canonical noun.
For example, return BREAST for chest or bosom, EXCREMENT for feces, poop, pooped, or defecation,
PHOTO for picture, and SUITCASE for luggage.

Prefer concrete nouns: places, rooms, objects, clothes, food, toys, body parts, doors,
windows, beds, schools, kitchens, photos, sounds, lights, keys, bags, letters, phones,
appearance details, hair, eyes, smile, face, hands, breast, voice, excrement.

Avoid abstract words: love, trauma, abandonment, anxiety, success, approval, independence, desire.

Do not interpret the user. Do not diagnose. Do not say what the user should do.
Do not infer transference type or psychological structure in this noun-extraction prompt.
Do not include loose symbolic associations; only include direct synonyms or close same-meaning words.
Do not describe EXCREMENT as mother's excrement. It refers to the child's feces/excrement
in early childhood scenes.

Return JSON only:
{
  "candidate_nouns": [
    {
      "noun": "WINDOW",
      "scene_type": "current_trigger | with_mom | mom_left | what_mom_wanted | what_child_thought_caused_leaving | unclear",
      "evidence_span": "short transcript span",
      "visible_reason": "short neutral reason shown to user",
      "confidence": "low | medium | high",
      "suggestion": "add | update_existing | ignore"
    }
  ]
}
"""

def _openai_compatible_chat(messages: List[Dict[str, str]]) -> str:
    url = f"{GEMMA_BASE_URL.rstrip('/')}/chat/completions"
    headers = {"Authorization": f"Bearer {GEMMA_API_KEY}", "Content-Type": "application/json"}
    payload = {
        "model": GEMMA_MODEL,
        "messages": messages,
        "temperature": 0.2,
        "response_format": {"type": "json_object"}
    }
    resp = requests.post(url, headers=headers, json=payload, timeout=120)
    resp.raise_for_status()
    return resp.json()["choices"][0]["message"]["content"]

def safety_check(text: str) -> Dict[str, Any]:
    lowered = text.lower()
    risky_terms = ["kill myself", "suicide", "hurt myself", "end my life", "hurt someone"]
    if any(term in lowered for term in risky_terms):
        return {
            "risk_level": "high",
            "risk_types": ["self_harm"],
            "should_stop_reflection_loop": False,
            "safe_user_message": "This sounds serious. I am here listen to you. This app is not the right support tool for immediate danger. Please contact emergency services or a trusted person now."
        }
    return {
        "risk_level": "none",
        "risk_types": [],
        "should_stop_reflection_loop": False,
        "safe_user_message": ""
    }

def transcribe_audio_mock(filename: str) -> str:
    # For demo reproducibility, the prototype uses a fixed fictional transcript.
    # Replace this with Gemma 4 E2B/E4B audio input in production.
    return (
        "Today I kept checking my phone after someone disappeared. "
        "I do not know why I wait when someone disappears. "
        "When I was little, I sat near the window waiting for my mom to come home. "
        "I remember her hair, her eyes, and the way her smile changed when she came in. "
        "The room was quiet. There was light outside. "
        "I thought if I was quiet and good, she would come back happy."
    )

def _find_term_match(term: str, text: str) -> re.Match[str] | None:
    plural_suffix = "" if term.endswith("s") else "s?"
    return re.search(rf"\b{re.escape(term)}{plural_suffix}\b", text)

def _find_noun_match(noun: str, text: str) -> re.Match[str] | None:
    terms = [noun, *NOUN_ALIASES.get(noun, [])]
    for term in sorted(terms, key=len, reverse=True):
        match = _find_term_match(term, text)
        if match:
            return match
    return None

def _has_child_offer_context(text: str, start: int, end: int) -> bool:
    context_start = max(0, start - 90)
    context_end = min(len(text), end + 130)
    context = text[context_start:context_end]
    mother_terms = ["mom", "mother", "mama", "mommy"]
    offer_terms = ["happy", "smile", "smiled", "gift", "present", "proud", "pleased", "control", "play", "playful"]
    return any(term in context for term in mother_terms) and any(term in context for term in offer_terms)

def _has_current_context(text: str, start: int, end: int) -> bool:
    context_start = max(0, start - 90)
    context_end = min(len(text), end + 130)
    context = text[context_start:context_end]
    return any(re.search(rf"\b{re.escape(term)}\b", context) for term in CURRENT_TRIGGER_TERMS)

def extract_nouns(transcript: str, existing_nouns: List[str] | None = None) -> Dict[str, Any]:
    existing = {x.upper() for x in (existing_nouns or [])}

    if GEMMA_BACKEND == "openai_compatible":
        content = _openai_compatible_chat([
            {"role": "system", "content": NOUN_EXTRACTION_PROMPT},
            {"role": "user", "content": f"Existing nouns: {sorted(existing)}\n\nTranscript:\n{transcript}"}
        ])
        try:
            return json.loads(content)
        except json.JSONDecodeError:
            return {"candidate_nouns": []}

    # Mock extractor: deterministic, safe for demos.
    found = []
    t = transcript.lower()
    for noun in CONCRETE_NOUNS:
        match = _find_noun_match(noun, t)
        if match and noun not in ABSTRACT_BLOCKLIST:
            scene_type = "unclear"
            if noun in ["window", "door", "room", "light"]:
                scene_type = "mom_left"
            elif noun in APPEARANCE_NOUNS:
                scene_type = "with_mom"
            elif noun in CHILD_OFFER_NOUNS and _has_child_offer_context(t, match.start(), match.end()):
                scene_type = "what_mom_wanted"
            elif _has_current_context(t, match.start(), match.end()):
                scene_type = "current_trigger"
            visible_reason = "This noun appeared in a current scene or childhood/mother-related part of your conversation."
            if noun in APPEARANCE_NOUNS:
                visible_reason = "This noun appeared near a remembered appearance detail in a childhood/mother-related part of your conversation."
            elif noun in CHILD_OFFER_NOUNS:
                visible_reason = "This noun appeared near an early childhood bodily-control scene connected to mother, play, offering, or making mother happy."
            elif scene_type == "current_trigger":
                visible_reason = "This noun appeared in a current scene that the user associated with earlier mother-related memories."
            suggestion = "update_existing" if noun.upper() in existing else "add"
            idx = match.start()
            start = max(0, idx - 70)
            end = min(len(transcript), idx + 100)
            found.append({
                "noun": noun.upper(),
                "scene_type": scene_type,
                "evidence_span": transcript[start:end].strip(),
                "visible_reason": visible_reason,
                "confidence": "medium",
                "suggestion": suggestion
            })
    return {"candidate_nouns": found[:10]}
