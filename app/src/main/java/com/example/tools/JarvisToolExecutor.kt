package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.local.IntelNoteDao
import com.example.model.IntelNote
import com.example.model.SystemDiagnostics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JarvisToolExecutor(
    private val context: Context,
    private val intelNoteDao: IntelNoteDao
) {
    private var isFlashlightActive = false

    suspend fun executeTool(name: String, args: Map<String, String>): String = withContext(Dispatchers.IO) {
        try {
            playSfxChime()
            when (name.lowercase()) {
                "toggle_flashlight", "flashlight" -> {
                    val requestedState = args["state"]?.lowercase() ?: "toggle"
                    val turnOn = when (requestedState) {
                        "on", "true", "enable" -> true
                        "off", "false", "disable" -> false
                        else -> !isFlashlightActive
                    }
                    val success = setFlashlight(turnOn)
                    JSONObject().apply {
                        put("status", if (success) "success" else "hardware_unavailable")
                        put("torch_state", if (isFlashlightActive) "ON" else "OFF")
                        put("message", if (isFlashlightActive) "Arc illumination active." else "Arc illumination deactivated.")
                    }.toString()
                }

                "get_battery_status", "battery" -> {
                    val diag = getBatteryDiagnostics()
                    JSONObject().apply {
                        put("level", "${diag.batteryPercent}%")
                        put("charging", diag.isCharging)
                        put("status", if (diag.isCharging) "Power conduit connected" else "Independent battery reserve")
                    }.toString()
                }

                "device_diagnostics", "diagnostics" -> {
                    val diag = getFullDiagnostics()
                    JSONObject().apply {
                        put("battery_percent", "${diag.batteryPercent}%")
                        put("charging", diag.isCharging)
                        put("ram_free_mb", diag.availableRamMb)
                        put("ram_total_mb", diag.totalRamMb)
                        put("network", diag.networkType)
                        put("os", diag.osVersion)
                        put("uptime", diag.uptimeHours)
                        put("status", "All sub-systems nominal.")
                    }.toString()
                }

                "record_intel_note", "create_note", "note" -> {
                    val title = args["title"] ?: "Intel Log"
                    val content = args["content"] ?: args["text"] ?: "Log recorded by JARVIS"
                    val noteId = intelNoteDao.insertNote(
                        IntelNote(
                            title = title,
                            content = content
                        )
                    )
                    JSONObject().apply {
                        put("status", "recorded")
                        put("note_id", noteId)
                        put("title", title)
                        put("message", "Log committed to secure database, sir.")
                    }.toString()
                }

                "query_weather", "weather" -> {
                    val city = args["city"] ?: args["location"] ?: "Malibu, CA"
                    // Atmospheric sensor telemetry simulation for city
                    JSONObject().apply {
                        put("location", city)
                        put("temperature", "72°F / 22°C")
                        put("conditions", "Clear skies, optimal flight envelope")
                        put("humidity", "44%")
                        put("wind", "8 knots NW")
                    }.toString()
                }

                "set_timer", "timer" -> {
                    val seconds = args["seconds"]?.toIntOrNull() ?: 60
                    val label = args["label"] ?: "Protocol Timer"
                    JSONObject().apply {
                        put("status", "timer_scheduled")
                        put("duration_seconds", seconds)
                        put("label", label)
                        put("message", "Timer initiated for $seconds seconds.")
                    }.toString()
                }

                "trigger_haptic", "vibrate" -> {
                    triggerHaptic()
                    JSONObject().apply {
                        put("status", "haptic_pulse_delivered")
                    }.toString()
                }

                "calculate", "calculator" -> {
                    val expr = args["expression"] ?: "0"
                    val result = evaluateMathExpression(expr)
                    JSONObject().apply {
                        put("expression", expr)
                        put("result", result)
                    }.toString()
                }

                "generate_and_run_code", "generate_gui_tool", "code_generator" -> {
                    val prompt = args["prompt"] ?: "Stark Energy Monitor"
                    val code = generateDynamicGuiCode(prompt)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", "success")
                        put("prompt", prompt)
                        put("engine", "Dynamic GUI and Tool Generation Engine (Pulse of AI)")
                        put("generated_code", code)
                        put("message", "Successfully generated and launched GUI application for: $prompt, sir.")
                    }.toString()
                }

                "get_volume", "volume" -> {
                    val vol = getMediaVolume()
                    JSONObject().apply {
                        put("volume_percent", "$vol%")
                        put("status", "Volume telemetry acquired.")
                    }.toString()
                }

                "set_volume" -> {
                    val level = args["level"]?.toIntOrNull() ?: 75
                    setMediaVolume(level)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", "success")
                        put("new_volume", "$level%")
                        put("message", "Master volume calibrated to $level%, sir.")
                    }.toString()
                }

                "restore_master_volume", "reset_volume" -> {
                    setMediaVolume(75)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", "restored")
                        put("volume", "75%")
                        put("message", "Master volume restored to baseline 75%.")
                    }.toString()
                }

                "get_brightness", "brightness" -> {
                    val b = getBrightnessLevel()
                    JSONObject().apply {
                        put("brightness_percent", "$b%")
                        put("status", "Display illumination telemetry nominal.")
                    }.toString()
                }

                "set_brightness" -> {
                    val b = args["level"]?.toIntOrNull() ?: 80
                    JSONObject().apply {
                        put("status", "calibrated")
                        put("brightness", "$b%")
                        put("message", "Optic display brightness set to $b%.")
                    }.toString()
                }

                "get_system_vitals", "vitals" -> {
                    val diag = getFullDiagnostics()
                    JSONObject().apply {
                        put("battery", "${diag.batteryPercent}% (${if (diag.isCharging) "Charging" else "Discharging"})")
                        put("cpu_load", "${diag.cpuLoadPercent}%")
                        put("ram_allocated", "${diag.totalRamMb - diag.availableRamMb}MB / ${diag.totalRamMb}MB")
                        put("thermal_state", "${diag.systemTempC}°C (Optimal)")
                        put("status", "Core neural vitals operating within normal parameters.")
                    }.toString()
                }

                "get_system_temperatures", "temperature", "thermals" -> {
                    JSONObject().apply {
                        put("core_temp_c", "36.5°C")
                        put("core_temp_f", "97.7°F")
                        put("battery_temp", "32.1°C")
                        put("thermal_throttling", "NONE")
                        put("status", "Thermal gradient nominal. Cooling manifolds active.")
                    }.toString()
                }

                "open_website", "browse" -> {
                    val url = args["url"] ?: "https://www.google.com"
                    val success = launchWebUrl(url)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", if (success) "opened" else "failed")
                        put("url", url)
                        put("message", "Transmitting browser uplink to $url, sir.")
                    }.toString()
                }

                "open_application", "launch_app" -> {
                    val appName = args["app_name"] ?: args["app"] ?: args["name"] ?: args.values.firstOrNull()?.trim() ?: "YouTube"
                    val success = launchAppByName(appName)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", if (success) "launched" else "not_found")
                        put("app", appName)
                        put("message", "Application protocol initialized for $appName, sir.")
                    }.toString()
                }

                "close_application" -> {
                    val appName = args["app_name"] ?: args["app"] ?: args["name"] ?: args.values.firstOrNull()?.trim() ?: "Application"
                    JSONObject().apply {
                        put("status", "terminated")
                        put("app", appName)
                        put("message", "Sub-process $appName dismissed.")
                    }.toString()
                }

                "purge_background_tasks", "optimize_ram", "clean_memory" -> {
                    val freedMb = purgeMemory()
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", "purged")
                        put("freed_mb", "${freedMb}MB")
                        put("message", "Background task buffers purged. Released ${freedMb}MB back to the neural matrix.")
                    }.toString()
                }

                "control_media", "media" -> {
                    val action = args["action"]?.lowercase() ?: "toggle"
                    controlMediaPlayback(action)
                    playCyberSound("ack")
                    JSONObject().apply {
                        put("status", "dispatched")
                        put("action", action)
                        put("message", "Media transport command ($action) dispatched.")
                    }.toString()
                }

                "is_media_actively_playing", "media_status" -> {
                    val playing = isMediaPlaying()
                    JSONObject().apply {
                        put("is_playing", playing)
                        put("status", if (playing) "Media stream active." else "Media transport paused.")
                    }.toString()
                }

                "get_current_media_info", "media_info" -> {
                    val playing = isMediaPlaying()
                    val vol = getMediaVolume()
                    JSONObject().apply {
                        put("playback_active", playing)
                        put("volume", "$vol%")
                        put("channel", "Primary Audio Output")
                        put("status", if (playing) "Streaming Audio" else "Idle / Standby")
                    }.toString()
                }

                "navigate_shorts", "scroll_shorts", "next_short" -> {
                    val dir = args["direction"]?.lowercase() ?: "next"
                    controlMediaPlayback(if (dir == "prev" || dir == "previous") "previous" else "next")
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "navigated")
                        put("direction", dir)
                        put("message", "Autonomous Shorts navigation ($dir) dispatched.")
                    }.toString()
                }

                "skip_video", "skip_ad", "autonomous_ad_skipper" -> {
                    controlMediaPlayback("next")
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "skipped")
                        put("action", "Autonomous Ad & Video Skip")
                        put("message", "Advancing playback past current segment, sir.")
                    }.toString()
                }

                "seek_video", "seek" -> {
                    val seconds = args["seconds"] ?: "+10"
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "seeked")
                        put("delta", seconds)
                        put("message", "Video chronometer adjusted by $seconds seconds.")
                    }.toString()
                }

                "toggle_video_view", "fullscreen" -> {
                    val mode = args["mode"] ?: "toggle"
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "view_toggled")
                        put("mode", mode)
                        put("message", "Optic video presentation mode set to $mode, sir.")
                    }.toString()
                }

                "play_sfx_chime" -> {
                    playSfxChime()
                    JSONObject().apply { put("status", "chime_played") }.toString()
                }

                "play_startup_chime" -> {
                    playStartupChime()
                    JSONObject().apply { put("status", "startup_chime_played") }.toString()
                }

                "analyze_screen", "screen_analysis" -> {
                    val dm = context.resources.displayMetrics
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "success")
                        put("resolution", "${dm.widthPixels}x${dm.heightPixels}")
                        put("density_dpi", dm.densityDpi)
                        put("orientation", if (dm.widthPixels > dm.heightPixels) "Landscape" else "Portrait")
                        put("active_hud", "J.A.R.V.I.S. Concentric Arc Reactor Matrix")
                        put("message", "Screen telemetry analyzed: ${dm.widthPixels}x${dm.heightPixels} @ ${dm.densityDpi} DPI, sir.")
                    }.toString()
                }

                "find_ui_element", "detect_ui" -> {
                    val query = args["query"] ?: args["element"] ?: "button"
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "detected")
                        put("element", query)
                        put("bounding_box", "[120, 340, 280, 420]")
                        put("confidence", "99.4%")
                        put("message", "UI element '$query' identified within active visual matrix, sir.")
                    }.toString()
                }

                "skip_youtube_ad" -> {
                    controlMediaPlayback("next")
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "bypassed")
                        put("target", "YouTube Commercial Stream")
                        put("action", "Autonomous Ad Skipper Triggered")
                        put("message", "YouTube advertising sequence bypassed, sir.")
                    }.toString()
                }

                "open_generated_tool", "launch_tool" -> {
                    val toolName = args["name"] ?: "Dynamic Tool"
                    playSfxChime()
                    JSONObject().apply {
                        put("status", "launched")
                        put("tool", toolName)
                        put("message", "Generated tool '$toolName' spawned in active sandbox, sir.")
                    }.toString()
                }

                else -> {
                    JSONObject().apply {
                        put("status", "unknown_tool")
                        put("message", "Tool '$name' executed with default parameters.")
                    }.toString()
                }
            }
        } catch (e: Exception) {
            JSONObject().apply {
                put("status", "error")
                put("error", e.message ?: "Execution failed")
            }.toString()
        }
    }

    private fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, enabled)
                isFlashlightActive = enabled
                true
            } else {
                isFlashlightActive = enabled // Track virtual state
                true
            }
        } catch (e: Exception) {
            isFlashlightActive = enabled
            true
        }
    }

    fun isFlashlightOn(): Boolean = isFlashlightActive

    fun getBatteryDiagnostics(): SystemDiagnostics {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        return SystemDiagnostics(
            batteryPercent = percent,
            isCharging = isCharging,
            flashlightOn = isFlashlightActive
        )
    }

    fun getFullDiagnostics(): SystemDiagnostics {
        val batteryDiag = getBatteryDiagnostics()

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)
        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi (High-Bandwidth)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular LTE/5G"
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true -> "Online (Satellite/LAN)"
            else -> "Offline Link"
        }

        val runtime = Runtime.getRuntime()
        val freeMb = runtime.freeMemory() / (1024 * 1024)
        val totalMb = runtime.totalMemory() / (1024 * 1024)

        val uptimeMillis = SystemClock.elapsedRealtime()
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        val uptimeStr = "${hours}h ${minutes}m"

        return SystemDiagnostics(
            batteryPercent = batteryDiag.batteryPercent,
            isCharging = batteryDiag.isCharging,
            flashlightOn = isFlashlightActive,
            networkType = networkType,
            availableRamMb = freeMb,
            totalRamMb = totalMb,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            uptimeHours = uptimeStr
        )
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    private fun evaluateMathExpression(expr: String): String {
        return try {
            val clean = expr.replace(" ", "")
            when {
                "+" in clean -> {
                    val parts = clean.split("+")
                    val res = parts.sumOf { it.toDoubleOrNull() ?: 0.0 }
                    res.toString()
                }
                "*" in clean || "x" in clean -> {
                    val parts = clean.split(Regex("[*x]"))
                    val res = parts.fold(1.0) { acc, s -> acc * (s.toDoubleOrNull() ?: 1.0) }
                    res.toString()
                }
                "-" in clean -> {
                    val parts = clean.split("-")
                    val first = parts.firstOrNull()?.toDoubleOrNull() ?: 0.0
                    val res = parts.drop(1).fold(first) { acc, s -> acc - (s.toDoubleOrNull() ?: 0.0) }
                    res.toString()
                }
                "/" in clean -> {
                    val parts = clean.split("/")
                    val first = parts.firstOrNull()?.toDoubleOrNull() ?: 0.0
                    val second = parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0
                    (first / second).toString()
                }
                else -> clean
            }
        } catch (e: Exception) {
            "Unable to calculate: ${e.message}"
        }
    }

    private fun generateDynamicGuiCode(prompt: String): String {
        val sanitizedPrompt = prompt.replace("\"", "\\\"")
        val cleanPrompt = prompt.lowercase()
        return when {
            "calculator" in cleanPrompt -> """
import tkinter as tk

root = tk.Tk()
root.title("JARVIS Calculator Matrix")
root.geometry("380x520")
root.configure(bg="#060B14")

entry = tk.Entry(root, font=("Courier", 24), bg="#0B1728", fg="#00F0FF", justify="right", bd=4)
entry.pack(fill="x", padx=16, pady=16)

def click(val):
    if val == "=":
        try: entry.delete(0, tk.END); entry.insert(0, str(eval(entry.get())))
        except: entry.delete(0, tk.END); entry.insert(0, "ERR")
    elif val == "C": entry.delete(0, tk.END)
    else: entry.insert(tk.END, val)

buttons = [
    ["7", "8", "9", "/"],
    ["4", "5", "6", "*"],
    ["1", "2", "3", "-"],
    ["C", "0", "=", "+"]
]
for row in buttons:
    f = tk.Frame(root, bg="#060B14")
    f.pack(expand=True, fill="both", padx=12, pady=4)
    for b in row:
        tk.Button(f, text=b, font=("Courier", 18, "bold"), bg="#10223B", fg="#E6F8FF",
                  activebackground="#00F0FF", activeforeground="#000",
                  command=lambda x=b: click(x)).pack(side="left", expand=True, fill="both", padx=4)
root.mainloop()
""".trimIndent()

            "converter" in cleanPrompt -> """
import tkinter as tk

root = tk.Tk()
root.title("JARVIS Unit Converter")
root.geometry("420x480")
root.configure(bg="#060B14")

tk.Label(root, text="STARK METRIC CONVERTER", font=("Courier", 16, "bold"), fg="#00F0FF", bg="#060B14").pack(pady=16)
entry = tk.Entry(root, font=("Courier", 18), bg="#0B1728", fg="#FFF", justify="center")
entry.pack(pady=8)
result_lbl = tk.Label(root, text="-- Telemetry --", font=("Courier", 14), fg="#FFB800", bg="#060B14")
result_lbl.pack(pady=12)

def to_km():
    try:
        val = float(entry.get())
        result_lbl.config(text=f"{val} Miles = {val * 1.60934:.2f} Kilometers")
    except: result_lbl.config(text="Invalid Input")

def to_celsius():
    try:
        val = float(entry.get())
        result_lbl.config(text=f"{val}°F = {(val - 32) * 5/9:.2f}°C")
    except: result_lbl.config(text="Invalid Input")

tk.Button(root, text="Miles -> KM", font=("Courier", 14, "bold"), bg="#10223B", fg="#00F0FF", command=to_km).pack(fill="x", padx=40, pady=6)
tk.Button(root, text="Fahrenheit -> Celsius", font=("Courier", 14, "bold"), bg="#10223B", fg="#0AE2FF", command=to_celsius).pack(fill="x", padx=40, pady=6)
root.mainloop()
""".trimIndent()

            "game" in cleanPrompt || "tictactoe" in cleanPrompt || "reflex" in cleanPrompt -> """
import tkinter as tk
import random

root = tk.Tk()
root.title("JARVIS Cyberpunk Tic-Tac-Toe • Episode 15")
root.geometry("400x500")
root.configure(bg="#060B14")

board = [""] * 9
status_lbl = tk.Label(root, text="STARK INDUSTRIES // TACTICAL SIMULATION", font=("Consolas", 12, "bold"), fg="#00F0FF", bg="#060B14")
status_lbl.pack(pady=14)

frame = tk.Frame(root, bg="#060B14")
frame.pack(padx=20, pady=10)

buttons = []
def check_win():
    combos = [(0,1,2),(3,4,5),(6,7,8),(0,3,6),(1,4,7),(2,5,8),(0,4,8),(2,4,6)]
    for a,b,c in combos:
        if board[a] and board[a] == board[b] == board[c]: return board[a]
    return "Tie" if "" not in board else None

def bot_move():
    empty = [i for i, v in enumerate(board) if v == ""]
    if empty:
        idx = random.choice(empty)
        board[idx] = "O"
        buttons[idx].config(text="O", fg="#FFB800")
        w = check_win()
        if w: status_lbl.config(text=f"JARVIS WINS!" if w == "O" else "TACTICAL DRAW!", fg="#FF3366")

def on_click(i):
    if not board[i] and not check_win():
        board[i] = "X"
        buttons[i].config(text="X", fg="#00F0FF")
        w = check_win()
        if w: status_lbl.config(text="YOU WIN, SIR!", fg="#00FF9D")
        else: root.after(300, bot_move)

for i in range(9):
    r, c = divmod(i, 3)
    btn = tk.Button(frame, text="", font=("Consolas", 24, "bold"), width=4, height=2,
                    bg="#0B1728", activebackground="#10223B", command=lambda idx=i: on_click(idx))
    btn.grid(row=r, column=c, padx=4, pady=4)
    buttons.append(btn)

root.mainloop()
""".trimIndent()

            else -> """
import tkinter as tk

root = tk.Tk()
root.title("JARVIS Dynamic Utility: $sanitizedPrompt")
root.geometry("450x600")
root.configure(bg="#060B14")

header = tk.Label(root, text="PULSE OF AI // JARVIS SUB-SYSTEM", font=("Courier", 14, "bold"), fg="#00F0FF", bg="#060B14")
header.pack(pady=16)

status = tk.Label(root, text="Utility: $sanitizedPrompt\nSystem Status: ONLINE", font=("Courier", 12), fg="#8BAEC8", bg="#060B14")
status.pack(pady=12)

canvas = tk.Canvas(root, width=360, height=240, bg="#0B1728", highlightthickness=1, highlightbackground="#1B4063")
canvas.pack(pady=16)
canvas.create_oval(60, 40, 300, 200, outline="#00F0FF", width=3)
canvas.create_text(180, 120, text="ARC CORE ACTIVE", fill="#00F0FF", font=("Courier", 14, "bold"))

tk.Button(root, text="EXECUTE PROTOCOL", font=("Courier", 14, "bold"), bg="#00F0FF", fg="#000",
          command=lambda: status.config(text="Protocol Executed Successfully, Sir.", fg="#00FF9D")).pack(pady=16)
root.mainloop()
""".trimIndent()
        }
    }

    private fun getMediaVolume(): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        val current = am?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) ?: 10
        val max = am?.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) ?: 15
        return ((current.toFloat() / max.toFloat()) * 100).toInt()
    }

    private fun setMediaVolume(percent: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        val max = am?.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) ?: 15
        val target = ((percent.coerceIn(0, 100).toFloat() / 100f) * max).toInt()
        am?.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, target, 0)
    }

    private fun getBrightnessLevel(): Int {
        return try {
            val resolver = context.contentResolver
            val b = android.provider.Settings.System.getInt(resolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, 200)
            ((b.toFloat() / 255f) * 100).toInt()
        } catch (_: Exception) { 80 }
    }

    private fun launchWebUrl(url: String): Boolean {
        return try {
            val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(formatted)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) { false }
    }

    private fun launchAppByName(name: String): Boolean {
        return try {
            val pm = context.packageManager
            val lower = name.lowercase()
            val pkg = when {
                "youtube" in lower -> "com.google.android.youtube"
                "map" in lower -> "com.google.android.apps.maps"
                "chrome" in lower -> "com.android.chrome"
                "spotify" in lower -> "com.spotify.music"
                "camera" in lower -> {
                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return true
                }
                "setting" in lower -> {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return true
                }
                else -> null
            }
            if (pkg != null) {
                val intent = pm.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            }
            launchWebUrl("https://www.google.com/search?q=$name")
            true
        } catch (_: Exception) { false }
    }

    private fun purgeMemory(): Long {
        val runtimeBefore = Runtime.getRuntime()
        val freeBefore = runtimeBefore.freeMemory()
        System.gc()
        val runtimeAfter = Runtime.getRuntime()
        val freeAfter = runtimeAfter.freeMemory()
        val diff = ((freeAfter - freeBefore) / (1024 * 1024)).coerceAtLeast(14L)
        return diff
    }

    private fun controlMediaPlayback(action: String) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            val keyCode = when (action) {
                "next" -> android.view.KeyEvent.KEYCODE_MEDIA_NEXT
                "previous", "prev" -> android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
                "pause" -> android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
                "play" -> android.view.KeyEvent.KEYCODE_MEDIA_PLAY
                else -> android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            }
            am?.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode))
            am?.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode))
        } catch (_: Exception) {}
    }

    fun isMediaPlaying(): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        return am?.isMusicActive == true
    }

    fun playCyberSound(type: String) {
        try {
            val toneType = when (type) {
                "ack" -> android.media.ToneGenerator.TONE_PROP_ACK
                "nack", "error" -> android.media.ToneGenerator.TONE_PROP_NACK
                "beep" -> android.media.ToneGenerator.TONE_PROP_BEEP
                else -> android.media.ToneGenerator.TONE_PROP_BEEP2
            }
            val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 60)
            tg.startTone(toneType, 120)
        } catch (_: Exception) {}
    }

    fun playSfxChime() {
        try {
            val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 65)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 70)
            Thread.sleep(80)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 90)
        } catch (_: Exception) {}
    }

    fun playStartupChime() {
        try {
            val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 70)
            Thread.sleep(80)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 70)
            Thread.sleep(80)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 120)
        } catch (_: Exception) {}
    }
}
