package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiResponse
import com.example.ai.JarvisAiEngine
import com.example.data.local.JarvisDatabase
import com.example.model.AssistantState
import com.example.model.ConsoleLogEntry
import com.example.model.IntelNote
import com.example.model.InteractionTurn
import com.example.model.LogType
import com.example.model.SystemDiagnostics
import com.example.model.ToolCallInfo
import com.example.model.TurnSender
import com.example.tools.JarvisToolExecutor
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class JarvisTab {
    HUD,
    CONSOLE,
    TOOLS,
    STUDIO,
    INTEL
}

data class JarvisUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val currentTab: JarvisTab = JarvisTab.HUD,
    val userSpokenTranscription: String = "",
    val jarvisSpeechChunk: String = "",
    val turns: List<InteractionTurn> = emptyList(),
    val activeToolCalls: List<ToolCallInfo> = emptyList(),
    val consoleLogs: List<ConsoleLogEntry> = emptyList(),
    val isModelSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val diagnostics: SystemDiagnostics = SystemDiagnostics(),
    val activeTimerSeconds: Int? = null,
    val activeTimerLabel: String = "",
    val currentAmplitude: Float = 0f,
    val textInput: String = "",
    val isApiKeyConfigured: Boolean = false,
    val voicePitch: Float = 0.95f,
    val voiceRate: Float = 1.02f
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getInstance(application)
    private val intelNoteDao = database.intelNoteDao()
    val intelNotes: StateFlow<List<IntelNote>> = intelNoteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val toolExecutor = JarvisToolExecutor(application, intelNoteDao)
    private val aiEngine = JarvisAiEngine(toolExecutor)
    private val speechManager = SpeechManager(application)
    private val ttsManager = TtsManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var idlePulseJob: Job? = null

    init {
        setupVoiceListeners()
        refreshDiagnostics()
        addConsoleLog("SYSTEM", "Stark Industries Neural Sub-Systems Initialized.", LogType.SYSTEM)
        addConsoleLog("SYSTEM", "■ [LISTENING] Dynamic Audio & Terminal Input Active...", LogType.STATUS)
        startIdlePulse()
        startPlaybackMonitor()
        viewModelScope.launch {
            delay(300)
            toolExecutor.playStartupChime()
            val greeting = getTimeGreeting()
            delay(400)
            addConsoleLog("JARVIS", "$greeting System online. All telemetry nominal.", LogType.JARVIS)
            ttsManager.speak("$greeting System online.")
        }
    }

    private fun startPlaybackMonitor() {
        viewModelScope.launch {
            var lastState: Boolean? = null
            while (true) {
                try {
                    val current = toolExecutor.isMediaPlaying()
                    if (current != lastState) {
                        lastState = current
                        if (current) {
                            addConsoleLog("SYSTEM", "■■ [MEDIA PLAYING DETECTED] Push-to-Talk Armed: Hold mic or type below.", LogType.STATUS)
                        } else {
                            addConsoleLog("SYSTEM", "■ [MEDIA PAUSED/IDLE] Hands-Free Mode Active. Speak freely.", LogType.STATUS)
                        }
                        _uiState.value = _uiState.value.copy(
                            diagnostics = _uiState.value.diagnostics.copy(isMediaPlaying = current)
                        )
                    }
                } catch (_: Exception) {}
                delay(1500)
            }
        }
    }

    private fun getTimeGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> "Good morning, sir."
            hour in 12..16 -> "Good afternoon, sir."
            else -> "Good evening, sir."
        }
    }

    private fun setupVoiceListeners() {
        speechManager.onSpeechPartialResult = { partial ->
            _uiState.value = _uiState.value.copy(userSpokenTranscription = partial)
        }

        speechManager.onSpeechFinalResult = { finalResult ->
            _uiState.value = _uiState.value.copy(
                userSpokenTranscription = finalResult,
                assistantState = AssistantState.PROCESSING
            )
            addConsoleLog("USER", finalResult, LogType.USER)
            processQuery(finalResult, isVoice = true)
        }

        speechManager.onSpeechError = { errorMsg ->
            if (_uiState.value.assistantState == AssistantState.LISTENING) {
                _uiState.value = _uiState.value.copy(assistantState = AssistantState.IDLE)
                addConsoleLog("SYSTEM", "Audio input alert: $errorMsg", LogType.SYSTEM)
            }
        }

        ttsManager.onSpeechStarted = {
            _uiState.value = _uiState.value.copy(
                isModelSpeaking = true,
                assistantState = AssistantState.SPEAKING
            )
            toolExecutor.playCyberSound("ack")
            addConsoleLog("JARVIS", "■ [JARVIS ANSWERING...]", LogType.STATUS)
        }

        ttsManager.onSpeechCompleted = {
            _uiState.value = _uiState.value.copy(
                isModelSpeaking = false,
                assistantState = AssistantState.IDLE,
                jarvisSpeechChunk = ""
            )
            addConsoleLog("SYSTEM", "■ [LISTENING] Awaiting instructions...", LogType.STATUS)
        }

        ttsManager.onInterrupted = {
            _uiState.value = _uiState.value.copy(
                isModelSpeaking = false,
                assistantState = AssistantState.INTERRUPTED,
                jarvisSpeechChunk = ""
            )
            toolExecutor.playCyberSound("nack")
            addConsoleLog("SYSTEM", "■ [INTERRUPTED] Audio stream canceled. User barge-in detected.", LogType.STATUS)
            viewModelScope.launch {
                delay(800)
                if (_uiState.value.assistantState == AssistantState.INTERRUPTED) {
                    _uiState.value = _uiState.value.copy(assistantState = AssistantState.IDLE)
                    addConsoleLog("SYSTEM", "■ [LISTENING] Awaiting instructions...", LogType.STATUS)
                }
            }
        }

        // Combine audio amplitudes for responsive visualizer
        viewModelScope.launch {
            combine(
                speechManager.rmsAmplitude,
                ttsManager.speechAmplitude,
                ttsManager.isSpeaking,
                speechManager.isListening
            ) { micAmp, ttsAmp, speaking, listening ->
                when {
                    speaking -> ttsAmp
                    listening -> micAmp
                    else -> 0.05f
                }
            }.collect { amplitude ->
                _uiState.value = _uiState.value.copy(currentAmplitude = amplitude)
            }
        }
    }

    private fun startIdlePulse() {
        idlePulseJob?.cancel()
        idlePulseJob = viewModelScope.launch {
            var phase = 0f
            while (true) {
                if (!_uiState.value.isModelSpeaking && !_uiState.value.isListening) {
                    phase += 0.08f
                    val pulse = 0.08f + kotlin.math.sin(phase).toFloat() * 0.04f
                    _uiState.value = _uiState.value.copy(currentAmplitude = pulse.coerceIn(0.04f, 0.2f))
                }
                delay(60)
            }
        }
    }

    fun toggleListening() {
        if (_uiState.value.isModelSpeaking) {
            // Barge-in: User speaks/taps while model is speaking -> Interrupt immediately!
            onUserInterrupt()
            return
        }

        if (_uiState.value.isListening) {
            speechManager.stopListening()
            _uiState.value = _uiState.value.copy(
                isListening = false,
                assistantState = AssistantState.IDLE
            )
            addConsoleLog("SYSTEM", "Microphone array placed on standby.", LogType.SYSTEM)
        } else {
            _uiState.value = _uiState.value.copy(
                isListening = true,
                userSpokenTranscription = "",
                assistantState = AssistantState.LISTENING
            )
            addConsoleLog("SYSTEM", "■ [LISTENING] Dynamic Audio Active...", LogType.STATUS)
            speechManager.startListening()
        }
    }

    /**
     * Exact implementation of the user's interruption logic:
     * if server_content.interrupted:
     *   while not audio_queue.empty(): audio_queue.get_nowait()
     *   model_is_speaking = False
     */
    fun onUserInterrupt() {
        ttsManager.interrupt()
        _uiState.value = _uiState.value.copy(
            isModelSpeaking = false,
            assistantState = AssistantState.INTERRUPTED,
            jarvisSpeechChunk = ""
        )
    }

    fun onTextInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(textInput = text)
    }

    fun submitTextQuery() {
        val query = _uiState.value.textInput.trim()
        if (query.isEmpty()) return
        _uiState.value = _uiState.value.copy(
            textInput = "",
            userSpokenTranscription = query,
            assistantState = AssistantState.PROCESSING
        )
        addConsoleLog("HUD", "■■ [HUD INPUT] $query", LogType.USER)
        processQuery(query, isVoice = false)
    }

    fun processQuickCommand(command: String) {
        _uiState.value = _uiState.value.copy(
            userSpokenTranscription = command,
            assistantState = AssistantState.PROCESSING
        )
        addConsoleLog("USER", command, LogType.USER)
        processQuery(command, isVoice = false)
    }

    private fun processQuery(query: String, isVoice: Boolean) {
        // Record User Turn
        val userTurn = InteractionTurn(
            sender = TurnSender.USER,
            text = query,
            isVoice = isVoice
        )
        val updatedTurns = _uiState.value.turns + userTurn
        _uiState.value = _uiState.value.copy(turns = updatedTurns)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(assistantState = AssistantState.CALLING_TOOLS)

            val response = aiEngine.processQuery(
                userPrompt = query,
                onToolInvoked = { toolInfo ->
                    val toolsList = _uiState.value.activeToolCalls.filter { it.callId != toolInfo.callId } + toolInfo
                    _uiState.value = _uiState.value.copy(activeToolCalls = toolsList)
                    addConsoleLog(
                        "TOOL",
                        "name=${toolInfo.name}, id=${toolInfo.callId}, status=${toolInfo.status}",
                        LogType.TOOL
                    )
                    if (toolInfo.result != null) {
                        addConsoleLog(
                            "TOOL",
                            "response={\"result\": ${toolInfo.result}}",
                            LogType.TOOL
                        )
                        handleToolSideEffects(toolInfo)
                    }
                },
                onChunkEmitted = { chunk ->
                    val accumulated = _uiState.value.jarvisSpeechChunk + (if (_uiState.value.jarvisSpeechChunk.isEmpty()) "" else " ") + chunk
                    _uiState.value = _uiState.value.copy(jarvisSpeechChunk = accumulated)
                }
            )

            when (response) {
                is AiResponse.Success -> {
                    addConsoleLog("JARVIS", response.fullText, LogType.JARVIS)

                    val jarvisTurn = InteractionTurn(
                        sender = TurnSender.JARVIS,
                        text = response.fullText,
                        isVoice = true
                    )
                    _uiState.value = _uiState.value.copy(
                        turns = _uiState.value.turns + jarvisTurn,
                        assistantState = AssistantState.SPEAKING
                    )
                    ttsManager.speak(response.fullText)
                }

                is AiResponse.Error -> {
                    val fallback = "I apologize, sir. An anomaly occurred in the neural bridge."
                    addConsoleLog("JARVIS", fallback, LogType.JARVIS)
                    ttsManager.speak(fallback)
                }
            }
        }
    }

    private fun handleToolSideEffects(toolInfo: ToolCallInfo) {
        try {
            val json = JSONObject(toolInfo.result ?: "{}")
            if (toolInfo.name.contains("flashlight")) {
                val state = json.optString("torch_state", "OFF") == "ON"
                _uiState.value = _uiState.value.copy(isFlashlightOn = state)
            } else if (toolInfo.name.contains("timer")) {
                val seconds = json.optInt("duration_seconds", 60)
                val label = json.optString("label", "Timer")
                startCountdownTimer(seconds, label)
            }
            refreshDiagnostics()
        } catch (_: Exception) {}
    }

    private fun startCountdownTimer(seconds: Int, label: String) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            activeTimerSeconds = seconds,
            activeTimerLabel = label
        )
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(activeTimerSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(activeTimerSeconds = null)
            addConsoleLog("SYSTEM", "Chronometer alert: $label has elapsed.", LogType.SYSTEM)
            ttsManager.speak("Sir, the $label has expired.")
        }
    }

    fun cancelActiveTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(activeTimerSeconds = null)
        addConsoleLog("SYSTEM", "Active timer cancelled.", LogType.SYSTEM)
    }

    fun toggleFlashlightDirect() {
        viewModelScope.launch {
            val newState = !_uiState.value.isFlashlightOn
            val result = toolExecutor.executeTool("toggle_flashlight", mapOf("state" to if (newState) "on" else "off"))
            val on = result.contains("ON")
            _uiState.value = _uiState.value.copy(isFlashlightOn = on)
            refreshDiagnostics()
            addConsoleLog("TOOL", "Manual override: Flashlight ${if (on) "ACTIVE" else "DEACTIVATED"}", LogType.TOOL)
        }
    }

    fun refreshDiagnostics() {
        val diag = toolExecutor.getFullDiagnostics()
        _uiState.value = _uiState.value.copy(
            diagnostics = diag,
            isFlashlightOn = toolExecutor.isFlashlightOn()
        )
    }

    fun switchTab(tab: JarvisTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun deleteNote(note: IntelNote) {
        viewModelScope.launch {
            intelNoteDao.deleteNote(note)
            addConsoleLog("SYSTEM", "Archived note deleted: ${note.title}", LogType.SYSTEM)
        }
    }

    fun updateVoiceConfig(pitch: Float, rate: Float) {
        _uiState.value = _uiState.value.copy(voicePitch = pitch, voiceRate = rate)
        ttsManager.updateVoiceSettings(pitch, rate)
    }

    fun updateCustomApiKey(key: String) {
        aiEngine.setApiKeyOverride(key)
        _uiState.value = _uiState.value.copy(isApiKeyConfigured = key.isNotBlank())
        addConsoleLog("SYSTEM", "Gemini neural uplink updated.", LogType.SYSTEM)
    }

    private fun addConsoleLog(tag: String, message: String, type: LogType) {
        val entry = ConsoleLogEntry(tag = tag, message = message, type = type)
        val logs = (_uiState.value.consoleLogs + entry).takeLast(100)
        _uiState.value = _uiState.value.copy(consoleLogs = logs)
    }

    fun clearConsoleLogs() {
        _uiState.value = _uiState.value.copy(consoleLogs = emptyList())
        addConsoleLog("SYSTEM", "Terminal buffer cleared. Sub-systems ready.", LogType.SYSTEM)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        timerJob?.cancel()
        idlePulseJob?.cancel()
    }
}
