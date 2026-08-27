package com.example.jarvis.ai

/**
 * Local model configuration for JARVIS.
 *
 * The model weights are intentionally not committed to GitHub because model
 * files are large binary assets. The app will load the model from its local
 * model directory once it has been installed on the device.
 */
object LocalModelConfig {
    const val MODEL_FILE_NAME = "Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm"
    const val MODEL_DISPLAY_NAME = "Gemma 3 1B IT (INT4)"

    fun modelPath(filesDir: String): String =
        "$filesDir/models/$MODEL_FILE_NAME"
}
