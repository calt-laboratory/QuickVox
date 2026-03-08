# QuickVox

### 1. Grocery List (Text-Based)

A simple grocery list where items are added via text input.
Supports adding, deleting individual items, and clearing the entire list with a confirmation dialog.
Items are persisted locally.

**Status:** Complete

**Planned improvements:**
- Highlight animation for newly added items

---

### 2. Grocery List (Voice-Based)

A grocery list powered by voice input using Android's SpeechRecognizer.
Speak multiple items in one go — the app splits on commas, "and", and "und" to create separate entries.
Includes runtime permission handling for microphone access.

**Status:** Functional, UI polish needed

**Planned improvements:**
- Wave/pulse animation on the record button to give visual feedback that recording is active
- Improved error handling and user feedback when speech recognition fails
- Duplicate detection — prevent adding items that are already on the list
- Highlight animation for newly added items

---

### 3. Voice Notes

A note-taking app powered by speech-to-text.
Record voice memos, have them transcribed, edit them, and export as PDF.

**Status:** Not yet implemented

**Planned features:**
- Privacy-focused speech-to-text using [Vosk](https://alphacephei.com/vosk/) on a self-hosted server — no third-party cloud services
- Server-side database for storing notes (title, transcription, timestamps)
- REST API for communication between app and server
- PDF export of individual notes
- Edit transcribed text before saving
- List view of all saved notes with search

---

### Vocabs

- **Suspend function** — a function marked with `suspend`. It can pause and resume without blocking the thread. 
  It just declares "I might wait for something."
  It can only be called from a coroutine (or another suspend function).
- **Coroutine** — the actual running environment that executes suspend functions. You create one with `launch {}`, `async {}`, etc.
