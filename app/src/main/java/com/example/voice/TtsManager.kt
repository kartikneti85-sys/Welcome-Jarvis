package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class TtsManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val audioQueue = ConcurrentLinkedQueue<String>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private var amplitudeJob: Job? = null

    var onSpeechStarted: (() -> Unit)? = null
    var onSpeechCompleted: (() -> Unit)? = null
    var onInterrupted: (() -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Configure signature JARVIS articulate Indian English tone as per Pulse of AI spec
            val indianLocale = Locale("en", "IN")
            val inResult = textToSpeech?.setLanguage(indianLocale)
            if (inResult == TextToSpeech.LANG_MISSING_DATA || inResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val ukResult = textToSpeech?.setLanguage(Locale.UK)
                if (ukResult == TextToSpeech.LANG_MISSING_DATA || ukResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.US)
                }
            }
            textToSpeech?.setPitch(0.95f) // Crisp, articulate tone
            textToSpeech?.setSpeechRate(1.02f)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startAmplitudeSimulation()
                    onSpeechStarted?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    stopAmplitudeSimulation()
                    _isSpeaking.value = false
                    onSpeechCompleted?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    stopAmplitudeSimulation()
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) {
            onSpeechCompleted?.invoke()
            return
        }

        // Add to audio queue
        audioQueue.offer(text)
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "jarvis_speech_${System.currentTimeMillis()}")

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }

    /**
     * Implements Barge-in / Interruption handling:
     * - Clears audio queue
     * - Halts TTS engine immediately
     * - Sets model_is_speaking = false
     */
    fun interrupt() {
        audioQueue.clear()
        stopAmplitudeSimulation()
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        onInterrupted?.invoke()
    }

    private fun startAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive && _isSpeaking.value) {
                // Generate dynamic realistic speech waveform amplitude (0.3 - 0.95)
                val base = 0.35f + Random.nextFloat() * 0.55f
                _speechAmplitude.value = base
                delay(80)
            }
            _speechAmplitude.value = 0f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _speechAmplitude.value = 0f
    }

    fun updateVoiceSettings(pitch: Float, rate: Float) {
        textToSpeech?.setPitch(pitch)
        textToSpeech?.setSpeechRate(rate)
    }

    fun shutdown() {
        stopAmplitudeSimulation()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
        textToSpeech = null
    }
}
