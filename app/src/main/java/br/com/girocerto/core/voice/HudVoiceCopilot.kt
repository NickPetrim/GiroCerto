package br.com.girocerto.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID

interface VoiceCopilot {
    fun speak(message: String)
    fun stop()
    fun shutdown()
}

class AndroidHudVoiceCopilot(context: Context) : VoiceCopilot {

    private var isReady: Boolean = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.forLanguageTag("pt-BR"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                isReady = false
            }
        }
    }

    override fun speak(message: String) {
        if (!isReady || message.isBlank()) return
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}

class NoOpVoiceCopilot : VoiceCopilot {
    val spokenMessages = mutableListOf<String>()
    override fun speak(message: String) {
        spokenMessages.add(message)
    }
    override fun stop() = Unit
    override fun shutdown() = Unit
}