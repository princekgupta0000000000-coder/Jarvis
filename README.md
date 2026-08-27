# JARVIS

A privacy-first, local-first personal AI assistant for Android, designed to understand text and voice, use controlled tools, remember user-approved information, and work offline where practical.

## Vision

JARVIS will eventually provide:

- Local LLM reasoning without a cloud LLM API
- Text and voice input/output
- Wake-word based active mode
- User-approved memory
- Timetable, class reminders, holidays and exam dates
- Attendance tracking and calculations
- Controlled Android actions such as opening apps and initiating calls/messages with confirmation where appropriate
- Basic local vision and OCR
- Proactive notifications
- Future desktop/laptop client using the same core architecture

## Development Principles

1. Local-first: keep inference and personal data on-device whenever practical.
2. Privacy-first: do not permanently store arbitrary microphone conversations.
3. Tool safety: the LLM proposes actions; a controlled tool layer executes them.
4. Modular architecture: Android UI, AI, voice, memory, tools and data remain separable.
5. Offline capability: core assistant functions should remain useful without internet.

## Current milestone

**Phase 0 — Project foundation**

Next: Android app skeleton and the first JARVIS home screen, followed by local LLM integration.

## Planned architecture

```text
Android UI
    |
    v
Assistant Core
    |
    +--> Local LLM
    +--> Memory Manager
    +--> Intent / Tool Router
              |
              +--> Timetable & Reminders
              +--> Attendance
              +--> Calendar / Exams
              +--> Android Actions
              +--> Vision / OCR
    |
    +--> Voice Pipeline
          +--> Speech-to-Text
          +--> Text-to-Speech
```

## Status

🚧 Early development
