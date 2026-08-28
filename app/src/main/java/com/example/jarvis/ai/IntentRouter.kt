package com.example.jarvis.ai

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentRouter {

    fun execute(context: Context, text: String): String? {

        val msg = text.lowercase()

        return when {

            msg.contains("youtube") -> {
                open(context, "com.google.android.youtube")
                "Opening YouTube"
            }

            msg.contains("whatsapp") -> {
                open(context, "com.whatsapp")
                "Opening WhatsApp"
            }

            msg.contains("chrome") -> {
                open(context, "com.android.chrome")
                "Opening Chrome"
            }

            msg.contains("camera") -> {
                val i = Intent("android.media.action.IMAGE_CAPTURE")
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
                "Opening Camera"
            }

            msg.contains("gmail") || msg.contains("email") -> {
                val i = Intent(Intent.ACTION_SENDTO)
                i.data = Uri.parse("mailto:")
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
                "Opening Gmail"
            }

            msg.contains("call") || msg.contains("phone") -> {

                val number = msg.filter { it.isDigit() }

                if (number.length >= 10) {

                    val i = Intent(Intent.ACTION_DIAL)
                    i.data = Uri.parse("tel:$number")
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)

                    "Calling $number"

                } else "Please say the phone number"

            }

            else -> null
        }
    }

    private fun open(context: Context, pkg: String) {

        val i = context.packageManager.getLaunchIntentForPackage(pkg)

        if (i != null) {

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)

        }

    }

}
