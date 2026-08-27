package com.example.jarvis.ai

import android.content.Context
import android.net.Uri
import java.io.File

object ModelManager {
    const val MODEL_FILE_NAME = "gemma3-1b-it-int4.task"

    fun modelFile(context: Context): File =
        File(context.filesDir, "models/$MODEL_FILE_NAME")

    fun isInstalled(context: Context): Boolean {
        val file = modelFile(context)
        return file.exists() && file.length() > 50_000_000L
    }

    fun ensureModelDirectory(context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }

    fun installFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val destination = modelFile(context)
            ensureModelDirectory(context)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output, bufferSize = 1024 * 1024)
                }
            } ?: return false

            if (destination.length() > 50_000_000L) true
            else {
                destination.delete()
                false
            }
        } catch (_: Exception) {
            modelFile(context).delete()
            false
        }
    }
}
