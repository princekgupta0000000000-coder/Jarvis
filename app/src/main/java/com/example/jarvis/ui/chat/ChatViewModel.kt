package com.example.jarvis.ui.chat

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.jarvis.ai.LocalLlmEngine

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val messages = mutableStateListOf<ChatMessage>()

    private val llm = LocalLlmEngine(application.applicationContext)
    private var modelReady = false

    init {
        llm.initialize { ready -> modelReady = ready }
    }

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return

        messages.add(ChatMessage(clean, true))

        if (!modelReady) {
            messages.add(ChatMessage(
                "My local brain is not loaded yet. Install the Gemma 3 1B model first.",
                false
            ))
            return
        }

        messages.add(ChatMessage("Thinking...", false))
        llm.generate(clean) { response ->
            val index = messages.indexOfFirst { !it.isUser && it.text == "Thinking..." }
            if (index >= 0) {
                messages[index] = ChatMessage(response, false)
            } else {
                messages.add(ChatMessage(response, false))
            }
        }
    }

    override fun onCleared() {
        llm.close()
        super.onCleared()
    }
}
