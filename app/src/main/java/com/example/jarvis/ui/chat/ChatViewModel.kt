package com.example.jarvis.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return

        messages.add(ChatMessage(clean, true))

        // Temporary local response.
        // This will be replaced by the on-device LLM engine.
        messages.add(
            ChatMessage(
                text = "I received: $clean",
                isUser = false
            )
        )
    }
}
