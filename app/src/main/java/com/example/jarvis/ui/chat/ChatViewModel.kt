package com.example.jarvis.ui.chat

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.jarvis.ai.LocalLlmEngine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val messages = mutableStateListOf<ChatMessage>()
    private val llm = LocalLlmEngine(application.applicationContext)
    private var modelReady = false

    init {
        messages.add(ChatMessage(greeting(), false))
        llm.initialize { ready -> modelReady = ready }
    }

    private fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "सुप्रभात। मैं JARVIS हूँ। बताइए, आज क्या करना है?"
            in 12..16 -> "नमस्कार। मैं JARVIS हूँ। मैं आपकी मदद के लिए तैयार हूँ।"
            in 17..20 -> "शुभ संध्या। JARVIS online है। बताइए।"
            else -> "शुभ रात्रि। JARVIS online है। मैं आपकी मदद के लिए तैयार हूँ।"
        }
    }

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        messages.add(ChatMessage(clean, true))

        val instant = quickResponse(clean)
        if (instant != null) {
            messages.add(ChatMessage(instant, false))
            return
        }

        if (!modelReady) {
            messages.add(ChatMessage("मेरा local brain अभी तैयार हो रहा है। थोड़ी देर बाद फिर पूछिए।", false))
            return
        }

        messages.add(ChatMessage("सोच रहा हूँ…", false))
        val prompt = """
            You are JARVIS, a helpful personal Android assistant.
            Reply naturally and concisely. If the user writes Hindi or Hinglish, reply in Hindi/Hinglish.
            Never say that you cannot provide further assistance unless the request truly requires unavailable capabilities.
            User: $clean
            Assistant:
        """.trimIndent()
        llm.generate(prompt) { response ->
            val cleanResponse = response
                .replace("<start_of_turn>assistant", "", ignoreCase = true)
                .replace("<end_of_turn>", "", ignoreCase = true)
                .trim()
            val index = messages.indexOfFirst { !it.isUser && it.text == "सोच रहा हूँ…" }
            if (index >= 0) messages[index] = ChatMessage(if (cleanResponse.isBlank()) "मैंने समझा, लेकिन जवाब तैयार नहीं कर पाया।" else cleanResponse, false)
            else messages.add(ChatMessage(cleanResponse, false))
        }
    }

    private fun quickResponse(text: String): String? {
        val q = text.lowercase(Locale.getDefault()).trim()
        return when {
            q in setOf("hi", "hello", "hey", "hii", "namaste", "नमस्ते", "हेलो") -> "नमस्ते। मैं JARVIS हूँ। बताइए, मैं आपके लिए क्या करूँ?"
            q.contains("your name") || q.contains("tumhara naam") || q.contains("तुम्हारा नाम") -> "मेरा नाम JARVIS है।"
            q.contains("time") || q.contains("समय") || q.contains("kitne baje") -> "अभी समय ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())} है।"
            q.contains("date") || q.contains("तारीख") || q.contains("aaj ki date") -> "आज ${SimpleDateFormat("dd MMMM yyyy", Locale("hi", "IN")).format(Date())} है।"
            q.contains("good morning") || q.contains("शुभ प्रभात") -> "सुप्रभात। आपका दिन अच्छा रहे।"
            q.contains("good night") || q.contains("शुभ रात्रि") -> "शुभ रात्रि। आराम से सोइए।"
            else -> null
        }
    }

    override fun onCleared() {
        llm.close()
        super.onCleared()
    }
}
