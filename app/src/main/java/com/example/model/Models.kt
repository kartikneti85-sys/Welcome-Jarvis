package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssistantState(val label: String, val hudTag: String) {
    IDLE("Standby", "■ [IDLE] Awaiting instructions..."),
    LISTENING("Listening", "■ [LISTENING] Awaiting instructions..."),
    PROCESSING("Thinking", "■ [PROCESSING] Analyzing neural matrix..."),
    CALLING_TOOLS("Executing Tools", "■ [TOOLS] Invoking protocol..."),
    SPEAKING("Answering", "■ [JARVIS ANSWERING...]"),
    INTERRUPTED("Interrupted", "■ [INTERRUPTED] Audio stream canceled")
}

enum class TurnSender {
    USER,
    JARVIS,
    SYSTEM,
    TOOL
}

data class InteractionTurn(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: TurnSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = true,
    val toolName: String? = null,
    val toolResult: String? = null
)

enum class ToolStatus {
    RUNNING,
    SUCCESS,
    FAILED
}

data class ToolCallInfo(
    val callId: String,
    val name: String,
    val args: Map<String, String> = emptyMap(),
    val result: String? = null,
    val status: ToolStatus = ToolStatus.RUNNING,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "intel_notes")
data class IntelNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogType {
    USER,
    JARVIS,
    TOOL,
    SYSTEM,
    STATUS
}

data class ConsoleLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tag: String,
    val message: String,
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
)

data class SystemDiagnostics(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val flashlightOn: Boolean = false,
    val networkType: String = "Online",
    val availableRamMb: Long = 2048,
    val totalRamMb: Long = 4096,
    val osVersion: String = "Android 15",
    val uptimeHours: String = "4h 22m",
    val volumePercent: Int = 75,
    val brightnessPercent: Int = 80,
    val systemTempC: Float = 36.5f,
    val isMediaPlaying: Boolean = false,
    val cpuLoadPercent: Int = 14
)
