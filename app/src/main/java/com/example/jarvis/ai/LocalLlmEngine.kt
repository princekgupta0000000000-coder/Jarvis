package com.example.jarvis.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.util.concurrent.Executors

class LocalLlmEngine(context: Context) {
    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor()
    private var inference: LlmInference? = null

    fun initialize(onReady: (Boolean) -> Unit) {
        executor.execute {
            try {
                val file: File = ModelManager.modelFile(appContext)
                if (!file.exists()) {
                    onReady(false)
                    return@execute
                }

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(file.absolutePath)
                    .build()

                inference = LlmInference.createFromOptions(appContext, options)
                onReady(true)
            } catch (_: Exception) {
                onReady(false)
            }
        }
    }

    fun generate(prompt: String, onResult: (String) -> Unit) {
        executor.execute {
            val engine = inference
            if (engine == null) {
                onResult("My local brain is not loaded yet. Please install the model first.")
                return@execute
            }

            try {
                onResult(engine.generateResponse(prompt))
            } catch (_: Exception) {
                onResult("I couldn't process that locally right now.")
            }
        }
    }

    fun close() {
        executor.execute { inference?.close() }
        executor.shutdown()
    }
}
