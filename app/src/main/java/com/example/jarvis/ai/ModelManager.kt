package com.example.jarvis.ai

import android.content.Context
import java.io.File

object ModelManager {
    const val MODEL_FILE_NAME = "gemma3-1b-it.task"

    fun modelFile(context: Context): File =
        File(context.filesDir, "models/$MODEL_FILE_NAME")

    fun isInstalled(context: Context): Boolean = modelFile(context).exists()

    fun ensureModelDirectory(context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }
}
