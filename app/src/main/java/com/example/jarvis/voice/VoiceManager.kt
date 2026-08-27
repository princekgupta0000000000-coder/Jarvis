package com.example.jarvis.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(
    context: Context,
    private val onState: (VoiceState) -> Unit,
    private val onText: (String) -> Unit,
    private val onError: (String) -> Unit = {}
) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) setHindiVoiceIfAvailable()
        }
    }

    private fun setHindiVoiceIfAvailable() {
        val hindi = Locale("hi", "IN")
        val availability = tts?.isLanguageAvailable(hindi) ?: TextToSpeech.LANG_NOT_SUPPORTED
        tts?.language = if (availability >= TextToSpeech.LANG_AVAILABLE) hindi else Locale.getDefault()
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError("Speech recognition is not available on this device.")
            onState(VoiceState.IDLE)
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { onState(VoiceState.LISTENING) }
                override fun onBeginningOfSpeech() { onState(VoiceState.LISTENING) }
                override fun onRmsChanged(rmsdB: Float) { }
                override fun onBufferReceived(buffer: ByteArray?) { }
                override fun onEndOfSpeech() { onState(VoiceState.THINKING) }
                override fun onError(error: Int) { onError("मैं सुन नहीं पाया। फिर से बोलिए।"); onState(VoiceState.IDLE) }
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) onText(text) else onState(VoiceState.IDLE)
                }
                override fun onPartialResults(partialResults: Bundle?) { }
                override fun onEvent(eventType: Int, params: Bundle?) { }
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer?.startListening(intent)
    }

    fun speak(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        onState(VoiceState.SPEAKING)
        if (!ttsReady) return
        setHindiVoiceIfAvailable()
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response")
    }

    fun stop() {
        recognizer?.destroy(); recognizer = null
        tts?.stop()
        onState(VoiceState.IDLE)
    }

    fun release() {
        recognizer?.destroy(); recognizer = null
        tts?.shutdown(); tts = null
    }
}
