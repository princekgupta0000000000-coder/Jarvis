# JARVIS Android Architecture

## High-level flow

```text
User
  |
  +--> Text input
  |
  +--> Wake word -> Speech-to-Text
              |
              v
        Assistant Core
              |
              +--> Context + Memory
              |
              v
          Local LLM
              |
       Intent / Tool Router
              |
      +-------+--------+---------+---------+
      |       |        |         |         |
   Schedule Attendance Calendar Android  Vision
   & Alerts           / Exams   Tools     / OCR
              |
              v
       Response Composer
              |
        +-----+-----+
        |           |
      Text        TTS
                    |
                 Speaker
```

## UI screens

1. Home — animated JARVIS orb, status, quick actions, text input.
2. Chat — conversation history and text interaction.
3. Schedule — day/week timetable and next-class card.
4. Attendance — subject statistics and weekly summary.
5. Calendar — holidays, exams and events.
6. Vision — camera/image analysis and OCR.
7. Memory — user-approved memories with edit/delete controls.
8. Settings — model, voice, wake word, notifications, permissions and privacy.

## Assistant states

```text
IDLE -> LISTENING -> PROCESSING -> EXECUTING -> SPEAKING -> IDLE
                         |
                         +-> ERROR -> IDLE
```

### UI animation plan

- IDLE: slow breathing orb.
- LISTENING: audio-reactive waveform/ring.
- PROCESSING: rotating particle/orbit indicator.
- EXECUTING: tool-specific progress indicator.
- SPEAKING: voice-reactive orb pulse.
- SUCCESS: short confirmation/check animation.
- ERROR: subtle warning/shake animation.

## Core modules

```text
app/          Android entry point and dependency wiring
ui/           Compose screens and animations
ai/           Local LLM, prompts, intent parsing and context
voice/        Speech-to-text, wake-word and text-to-speech
memory/       User-approved long/short-term memory
tools/        Controlled Android actions
schedule/     Timetable and next-class calculation
attendance/   Attendance data and calculations
calendar/     Holidays, exams and events
vision/       Local image analysis and OCR
data/         Local database and repositories
core/         Shared models, utilities and state
```

## Tool execution rule

The LLM must not directly receive unrestricted Android/system access. It produces a structured intent/tool request. A controlled tool layer validates parameters and permissions before execution.

Example:

```text
"Call Mom"
   -> LLM: CALL_CONTACT(contact="Mom")
   -> Tool Router: validate
   -> Android action: open/prepare call
   -> JARVIS: confirmation/result
```

## Privacy

Microphone input is processed for the active interaction. Arbitrary background conversations should not become permanent memory. Persistent memory is created only through explicit user intent or clearly defined assistant actions.
