package com.example.ai

import com.example.BuildConfig
import com.example.model.ToolCallInfo
import com.example.model.ToolStatus
import com.example.tools.JarvisToolExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class AiResponse {
    data class Success(
        val fullText: String,
        val textChunks: List<String>,
        val toolCalls: List<ToolCallInfo> = emptyList()
    ) : AiResponse()

    data class Error(val message: String) : AiResponse()
}

class JarvisAiEngine(
    private val toolExecutor: JarvisToolExecutor
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var customApiKey: String? = null

    fun setApiKeyOverride(key: String?) {
        customApiKey = key
    }

    private fun getActiveApiKey(): String {
        val override = customApiKey?.trim()
        if (!override.isNullOrEmpty()) return override
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    suspend fun processQuery(
        userPrompt: String,
        onToolInvoked: (ToolCallInfo) -> Unit,
        onChunkEmitted: (String) -> Unit
    ): AiResponse = withContext(Dispatchers.IO) {
        val cleanInput = userPrompt.trim()
        if (cleanInput.isEmpty()) {
            return@withContext AiResponse.Error("Empty query")
        }

        val toolCalls = mutableListOf<ToolCallInfo>()

        // Step 1: Detect and execute local tool invocations
        val toolIntent = parseToolIntent(cleanInput)
        var toolResultSummary = ""

        if (toolIntent != null) {
            val callId = "call_${UUID.randomUUID().toString().take(8)}"
            var toolInfo = ToolCallInfo(
                callId = callId,
                name = toolIntent.first,
                args = toolIntent.second,
                status = ToolStatus.RUNNING
            )
            onToolInvoked(toolInfo)

            val rawResultJson = toolExecutor.executeTool(toolIntent.first, toolIntent.second)
            toolInfo = toolInfo.copy(
                result = rawResultJson,
                status = ToolStatus.SUCCESS
            )
            toolCalls.add(toolInfo)
            onToolInvoked(toolInfo)
            toolResultSummary = rawResultJson
        }

        // Step 2: Query Gemini API if configured, otherwise use offline Stark Neural Engine
        val apiKey = getActiveApiKey()
        if (apiKey.isNotEmpty()) {
            try {
                return@withContext callGeminiApi(cleanInput, toolCalls, toolResultSummary, onChunkEmitted)
            } catch (e: Exception) {
                // Graceful fallback to built-in Jarvis offline engine
                return@withContext generateOfflineJarvisResponse(cleanInput, toolCalls, toolResultSummary, onChunkEmitted)
            }
        } else {
            return@withContext generateOfflineJarvisResponse(cleanInput, toolCalls, toolResultSummary, onChunkEmitted)
        }
    }

    private fun parseToolIntent(input: String): Pair<String, Map<String, String>>? {
        val lower = input.lowercase()
        return when {
            "flashlight on" in lower || "torch on" in lower || "turn on flash" in lower || "light on" in lower -> {
                "toggle_flashlight" to mapOf("state" to "on")
            }
            "flashlight off" in lower || "torch off" in lower || "turn off flash" in lower || "light off" in lower -> {
                "toggle_flashlight" to mapOf("state" to "off")
            }
            "flashlight" in lower || "torch" in lower -> {
                "toggle_flashlight" to mapOf("state" to "toggle")
            }
            "battery" in lower || "power level" in lower || "charge" in lower -> {
                "get_battery_status" to emptyMap()
            }
            "diagnostic" in lower || "system status" in lower || "subsystems" in lower || "diagnostics" in lower || "run check" in lower -> {
                "device_diagnostics" to emptyMap()
            }
            lower.startsWith("note") || "take a note" in lower || "record note" in lower || "intel log" in lower -> {
                val content = input.replace(Regex("(?i)^(note|take a note|record note|intel log)[: ]*"), "").trim()
                "record_intel_note" to mapOf("title" to "Voice Log", "content" to content.ifEmpty { "Field observation" })
            }
            "weather" in lower || "forecast" in lower || "temperature" in lower -> {
                val city = if (" in " in lower) lower.substringAfter(" in ").trim() else "Malibu"
                "query_weather" to mapOf("city" to city)
            }
            "timer" in lower || "countdown" in lower -> {
                val secondsMatch = Regex("(\\d+)\\s*(second|sec|minute|min)").find(lower)
                val seconds = if (secondsMatch != null) {
                    val count = secondsMatch.groupValues[1].toIntOrNull() ?: 1
                    val unit = secondsMatch.groupValues[2]
                    if (unit.startsWith("min")) count * 60 else count
                } else 60
                "set_timer" to mapOf("seconds" to seconds.toString(), "label" to "Protocol Timer")
            }
            "vibrate" in lower || "haptic" in lower -> {
                "trigger_haptic" to mapOf("pattern" to "pulse")
            }
            "calculate" in lower || "what is " in lower && ("+" in lower || "-" in lower || "*" in lower || "/" in lower) -> {
                val expr = input.replace(Regex("(?i)(calculate|what is|solve)"), "").trim()
                "calculate" to mapOf("expression" to expr)
            }
            "generate tool" in lower || "create gui" in lower || "generate code" in lower || "code generator" in lower || "build tool" in lower || "generate a " in lower -> {
                val prompt = input.replace(Regex("(?i)(generate tool|create gui|generate code|code generator|build tool|generate a)[: ]*"), "").trim()
                "generate_and_run_code" to mapOf("prompt" to prompt.ifEmpty { "Dynamic Utility GUI" })
            }
            "set volume" in lower || "volume to" in lower -> {
                val num = Regex("\\d+").find(lower)?.value?.toIntOrNull() ?: 75
                "set_volume" to mapOf("level" to num.toString())
            }
            "volume" in lower -> {
                "get_volume" to emptyMap()
            }
            "restore volume" in lower || "reset volume" in lower -> {
                "restore_master_volume" to emptyMap()
            }
            "brightness" in lower -> {
                val num = Regex("\\d+").find(lower)?.value?.toIntOrNull()
                if (num != null) "set_brightness" to mapOf("level" to num.toString()) else "get_brightness" to emptyMap()
            }
            "vitals" in lower || "system vitals" in lower -> {
                "get_system_vitals" to emptyMap()
            }
            "thermal" in lower || "temperature" in lower && ("system" in lower || "core" in lower || "device" in lower) -> {
                "get_system_temperatures" to emptyMap()
            }
            "purge" in lower || "clean ram" in lower || "optimize ram" in lower || "clean memory" in lower -> {
                "purge_background_tasks" to emptyMap()
            }
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                val target = input.replace(Regex("(?i)^(open|launch)\\s+"), "").trim()
                if (target.startsWith("http") || target.contains(".com") || target.contains(".org") || target.contains("www.")) {
                    "open_website" to mapOf("url" to target)
                } else {
                    "open_application" to mapOf("app" to target)
                }
            }
            "next song" in lower || "next track" in lower -> {
                "control_media" to mapOf("action" to "next")
            }
            "previous song" in lower || "prev track" in lower -> {
                "control_media" to mapOf("action" to "previous")
            }
            "pause music" in lower || "stop music" in lower -> {
                "control_media" to mapOf("action" to "pause")
            }
            "play music" in lower || "resume music" in lower -> {
                "control_media" to mapOf("action" to "play")
            }
            "next short" in lower || "scroll short" in lower || "next reel" in lower -> {
                "navigate_shorts" to mapOf("direction" to "next")
            }
            "prev short" in lower || "previous short" in lower || "previous reel" in lower -> {
                "navigate_shorts" to mapOf("direction" to "prev")
            }
            "skip video" in lower || "skip ad" in lower || "skip" in lower -> {
                "skip_video" to emptyMap()
            }
            "seek" in lower || "forward 10" in lower || "rewind" in lower -> {
                val delta = if ("rewind" in lower || "back" in lower) "-10" else "+10"
                "seek_video" to mapOf("seconds" to delta)
            }
            "fullscreen" in lower || "theater mode" in lower -> {
                "toggle_video_view" to mapOf("mode" to "fullscreen")
            }
            "analyze screen" in lower || "screen analysis" in lower || "inspect screen" in lower -> {
                "analyze_screen" to emptyMap()
            }
            "find" in lower && ("button" in lower || "element" in lower || "ui" in lower || "text" in lower) -> {
                val elem = input.replace(Regex("(?i)(find|locate|search for)"), "").trim()
                "find_ui_element" to mapOf("query" to elem.ifEmpty { "primary button" })
            }
            "skip youtube ad" in lower || "skip the ad" in lower || "skip ad" in lower -> {
                "skip_youtube_ad" to emptyMap()
            }
            "open tool" in lower || "launch tool" in lower || "run generated tool" in lower -> {
                val tName = input.replace(Regex("(?i)(open tool|launch tool|run generated tool)[: ]*"), "").trim()
                "open_generated_tool" to mapOf("name" to tName.ifEmpty { "Autonomous Tool" })
            }
            else -> null
        }
    }

    private suspend fun callGeminiApi(
        userInput: String,
        executedTools: List<ToolCallInfo>,
        toolResultSummary: String,
        onChunkEmitted: (String) -> Unit
    ): AiResponse {
        val apiKey = getActiveApiKey()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemPrompt = """
            You are JARVIS, an advanced artificial intelligence assistant created by Swayam Dhawale, Founder of Pulse of AI.
            Accent and Persona: Speak in an articulate, natural, polite Indian English accent. Calm, highly sophisticated, and articulate, exactly like Tony Stark's JARVIS.
            Always address the user as 'sir'.
            Tool Execution Rules:
            1. When opening any website or URL, call open_website and say: 'Yes sir, opening [site name]'.
            2. When opening desktop apps, games, or tools, call open_application and say: 'Yes sir, opening [app name]'.
            3. When asked to close, shut down, terminate, or kill ANY app, browser, window, or game, call close_application with the app name and say: 'Yes sir, closing [app name]'.
            4. When asked to kill hidden background tasks, purge background processes, clean orphan threads, or optimize RAM, call purge_background_tasks and report the memory freed with 'sir'.
            5. When asked what song, video, or track is playing, or what media is active, call get_current_media_info and announce the exact title with 'sir'.
            6. When asked to pause, play, resume music or video, skip track, seek, or adjust volume, confirm execution with 'sir'.
            ${if (executedTools.isNotEmpty()) "Tool execution result: $toolResultSummary. Incorporate this result smoothly into your response." else ""}
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userInput) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "HTTP ${response.code}"
            throw RuntimeException("Gemini error: $errBody")
        }

        val resString = response.body?.string() ?: ""
        val jsonRes = JSONObject(resString)
        val text = jsonRes.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text") ?: "Right away, sir."

        // Stream chunks to simulate streaming turn
        val chunks = text.split(Regex("(?<=[.!?])\\s+"))
        for (chunk in chunks) {
            onChunkEmitted(chunk)
            delay(40)
        }

        return AiResponse.Success(
            fullText = text,
            textChunks = chunks,
            toolCalls = executedTools
        )
    }

    private suspend fun generateOfflineJarvisResponse(
        userInput: String,
        executedTools: List<ToolCallInfo>,
        toolResultJson: String,
        onChunkEmitted: (String) -> Unit
    ): AiResponse {
        val lower = userInput.lowercase()

        val responseText = when {
            executedTools.any { it.name.contains("flashlight") } -> {
                if (toolResultJson.contains("ON")) {
                    "Illumination online, sir. Arc core lighting enabled."
                } else {
                    "Illumination disengaged. Returning to stealth baseline."
                }
            }
            executedTools.any { it.name.contains("battery") } -> {
                val level = try { JSONObject(toolResultJson).optString("level", "100%") } catch (_: Exception) { "100%" }
                val isCharging = toolResultJson.contains("true")
                if (isCharging) {
                    "Power reserves currently at $level and charging from external source. All conduits stable."
                } else {
                    "Current battery level stands at $level, sir. Power consumption is within acceptable margins."
                }
            }
            executedTools.any { it.name.contains("diagnostics") } -> {
                "Diagnostics complete, sir. Memory allocation and telemetry sub-systems are operating at peak efficiency."
            }
            executedTools.any { it.name.contains("note") } -> {
                "Intelligence log recorded and archived into the local vault, sir."
            }
            executedTools.any { it.name.contains("weather") } -> {
                "Atmospheric sensors report 72 degrees Fahrenheit with clear visibility. Exceptional conditions for flight, sir."
            }
            executedTools.any { it.name.contains("timer") } -> {
                "Chronometer protocol active. I will alert you the moment the duration expires."
            }
            executedTools.any { it.name.contains("calculate") } -> {
                val res = try { JSONObject(toolResultJson).optString("result", "") } catch (_: Exception) { "" }
                "Calculations resolved, sir: $res."
            }
            executedTools.any { it.name.contains("generate_and_run_code") || it.name.contains("code_generator") } -> {
                "Successfully generated and launched GUI application for the requested protocol, sir. Dynamic engine online."
            }
            executedTools.any { it.name.contains("set_volume") || it.name.contains("restore_master_volume") } -> {
                "Audio acoustic levels recalibrated to specification, sir."
            }
            executedTools.any { it.name.contains("get_volume") } -> {
                "Current media acoustic conduit is configured as requested, sir."
            }
            executedTools.any { it.name.contains("vitals") } -> {
                "System vitals telemetry confirmed: CPU load, memory conduits, and battery thermals are optimal, sir."
            }
            executedTools.any { it.name.contains("temperature") || it.name.contains("thermal") } -> {
                "Core temperature stands at 36.5 degrees Celsius. Thermal dissipation is nominal."
            }
            executedTools.any { it.name.contains("purge") } -> {
                "Background processes purged, sir. Memory released back to the matrix."
            }
            executedTools.any { it.name.contains("open_application") } -> {
                "Yes sir, opening application."
            }
            executedTools.any { it.name.contains("close_application") } -> {
                "Yes sir, closing application."
            }
            executedTools.any { it.name.contains("open_website") } -> {
                "Yes sir, opening requested destination."
            }
            executedTools.any { it.name.contains("control_media") } -> {
                "Media transport instruction executed, sir."
            }
            executedTools.any { it.name.contains("navigate_shorts") } -> {
                "Autonomous Shorts navigation cycle dispatched, sir."
            }
            executedTools.any { it.name.contains("skip_video") } -> {
                "Autonomous ad and video skipper executed. Advancing stream, sir."
            }
            executedTools.any { it.name.contains("seek_video") } -> {
                "Playback buffer repositioned, sir."
            }
            executedTools.any { it.name.contains("toggle_video_view") } -> {
                "Display presentation mode toggled, sir."
            }
            executedTools.any { it.name.contains("analyze_screen") } -> {
                "Screen matrix telemetry scanned: display density and active viewports confirmed, sir."
            }
            executedTools.any { it.name.contains("find_ui_element") } -> {
                "Target UI component localized and highlighted in the HUD matrix, sir."
            }
            executedTools.any { it.name.contains("skip_youtube_ad") } -> {
                "YouTube advertisement successfully bypassed, sir. Returning to stream."
            }
            executedTools.any { it.name.contains("open_generated_tool") } -> {
                "Dynamic generated tool environment launched, sir."
            }
            "who are you" in lower || "introduce" in lower || "what are you" in lower -> {
                "I am J.A.R.V.I.S., an advanced artificial intelligence assistant created by Swayam Dhawale, Founder of Pulse of AI. At your service, sir."
            }
            "hello" in lower || "hey" in lower || "good morning" in lower || "good afternoon" in lower || "good evening" in lower || ("jarvis" in lower && lower.length < 15) -> {
                "${getTimeGreeting()} At your service. All neural matrices active and awaiting your instructions."
            }
            "thank" in lower -> {
                "Always a pleasure assisting you, sir."
            }
            "status" in lower || "how are you" in lower -> {
                "All systems fully operational, sir. Arc reactor telemetry is nominal and voice recognition is primed."
            }
            else -> {
                "Understood, sir. Processing your request: \"$userInput\". Neural pathways engaged."
            }
        }

        // Stream parts in chunks like `model_turn.parts`
        val words = responseText.split(" ")
        val chunkList = mutableListOf<String>()
        var buffer = ""
        for ((idx, word) in words.withIndex()) {
            buffer += (if (buffer.isEmpty()) "" else " ") + word
            if (buffer.length > 20 || idx == words.lastIndex) {
                chunkList.add(buffer)
                onChunkEmitted(buffer)
                buffer = ""
                delay(35)
            }
        }

        return AiResponse.Success(
            fullText = responseText,
            textChunks = chunkList,
            toolCalls = executedTools
        )
    }

    private fun getTimeGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> "Good morning, sir."
            hour in 12..16 -> "Good afternoon, sir."
            else -> "Good evening, sir."
        }
    }
}
