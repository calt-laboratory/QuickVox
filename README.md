# QuickVox

### 1. Grocery List (Text-Based)

A simple grocery list where items are added via text input.
Supports adding, deleting individual items, and clearing the entire list with a confirmation dialog.
Items are persisted locally.

**Status:** Complete

**Planned improvements:**
- ... 

---

### 2. Grocery List (Voice-Based)

A grocery list powered by voice input using Android's SpeechRecognizer.
Speak multiple items in one go — the app splits on commas, "and", and "und" to create separate entries.
Includes runtime permission handling for microphone access.

**Status:** Functional, UI polish needed

**Planned improvements:**
- Improved error handling and user feedback when speech recognition fails

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

#### Coroutine

The actual running environment that executes suspend functions.
You create one with `launch {}`, `async {}`, etc.

---

#### dB (Decibel)

A logarithmic unit for measuring loudness. Small number differences represent large loudness
differences (e.g. +10 dB sounds roughly twice as loud). In Android's `onRmsChanged(rmsdB: Float)`,
the value is the current microphone loudness already expressed in decibels.

---

#### Min-Max Scaling (Min-Max Normalization)

A technique to rescale a value into a fixed range of 0 to 1, based on the observed minimum
and maximum values.

```
        x - x_min
x' = ─────────────────
      x_max - x_min
```

Where `x` = current value, `x'` = normalized result (0 to 1).

Example with loudness values where min=2 and max=8:

| Raw value | Calculation | Normalized |
|-----------|-------------|------------|
| 2 (quietest) | (2-2) / (8-2) | 0.0 |
| 5 (medium) | (5-2) / (8-2) | 0.5 |
| 8 (loudest) | (8-2) / (8-2) | 1.0 |

In the WaveVisualizer, we use this to convert device-specific dB values (which could be any range)
into a 0–1 value that controls the bar heights. The min and max are tracked dynamically during
each recording session, so the visualizer adapts to any device automatically.

---

#### RMS (Root Mean Square)

A way to measure the "average loudness" of an audio signal.

**What is a Sample?**
A sample is a single measurement of the sound wave's air pressure at one point in time.
A microphone takes thousands of these measurements per second (e.g. 44,100 times per second
for CD quality). Each individual measurement is one sample. A single spoken word like "Milch"
consists of thousands of samples.

**Why not just average the samples?**
Audio samples oscillate between positive and negative values (the sound wave goes up and down),
so a simple average would be ~0. RMS fixes this by:

1. Squaring each sample (makes all values positive)
2. Taking the mean of all squared values
3. Taking the square root to get back to the original scale

Formula: `RMS = √(1/n × Σxᵢ²)`

**The full pipeline in Android:**
```
Microphone records audio
  → thousands of samples per second (e.g. [0.2, -0.5, 0.8, -0.3, ...])
  → Android takes a batch of these samples
  → calculates ONE RMS value (average loudness of that batch)
  → converts it to decibels (dB)
  → delivers it to onRmsChanged as a Float
```

`onRmsChanged` is called multiple times per second — not once per word or once per sample.
Android handles the entire calculation; we just receive the final dB value.

---

#### `return@Label`

A labeled return that exits only the surrounding lambda, not the whole function.
In Kotlin, `return` inside a lambda would exit the **entire enclosing function** by default.

`return@Button` means "exit only the `onClick` lambda of this `Button`."
The label name matches the function the lambda is passed to (e.g. `Button`, `forEach`, `launch`).

Use it whenever you want to do an early exit from a lambda (like `return` in a regular function)
without accidentally returning from the outer function.

---

#### Sample (Audio)

A single measurement of the sound wave's air pressure at one point in time. The microphone
doesn't record a continuous wave — it takes discrete snapshots. The number of snapshots per
second is the **sample rate** (e.g. 44,100 Hz = 44,100 samples per second). More samples
per second = more accurate representation of the original sound. A single spoken word consists
of thousands of samples.

---

#### Suspend function

A function marked with `suspend`. It can pause and resume without blocking the thread.
It just declares "I might wait for something."
It can only be called from a coroutine (or another suspend function).
