package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemDiagnostics
import com.example.model.ToolCallInfo
import com.example.model.ToolStatus
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JarvisToolMatrixView(
    tools: List<ToolCallInfo>,
    diagnostics: SystemDiagnostics,
    onTestTool: (String) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Hardware Telemetry Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STARK TELEMETRY MATRIX",
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onRefreshDiagnostics,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("refresh_diag_button")
                    ) {
                        Text("REFRESH", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = JarvisCyan)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "BATTERY", value = "${diagnostics.batteryPercent}% (${if (diagnostics.isCharging) "Charging" else "Unplugged"})")
                    TelemetryItem(label = "RAM FREE", value = "${diagnostics.availableRamMb}MB / ${diagnostics.totalRamMb}MB")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "NETWORK", value = diagnostics.networkType)
                    TelemetryItem(label = "TORCH", value = if (diagnostics.flashlightOn) "ACTIVE" else "OFF")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "THERMALS", value = "${diagnostics.systemTempC}°C (Nominal)")
                    TelemetryItem(label = "CPU LOAD", value = "${diagnostics.cpuLoadPercent}%")
                }
            }
        }

        // Test Tool Execution Actions
        Text(
            text = "INVOKE PROTOCOL TOOLS",
            color = JarvisTextSecondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val testActions = listOf(
                "Toggle Torch" to "Turn on flashlight",
                "System Vitals" to "Get system vitals",
                "Core Thermals" to "What is the core temperature?",
                "Purge Memory" to "Purge background tasks and clean RAM",
                "Skip Video / Ad" to "Skip video",
                "Next Short" to "Next short",
                "Seek +10s" to "Forward 10 seconds",
                "Fullscreen" to "Toggle video fullscreen",
                "Set Volume 80%" to "Set volume to 80",
                "Reset Volume" to "Restore master volume",
                "Play/Pause Media" to "Play music",
                "Open Camera" to "Open camera",
                "Open YouTube" to "Open YouTube",
                "Generate GUI Tool" to "Generate a dynamic GUI tool for Stark Energy Monitor",
                "Build Calculator" to "Generate a calculator GUI tool",
                "Weather Malibu" to "Atmospheric weather in Malibu",
                "Start 30s Timer" to "Set timer for 30 seconds",
                "Haptic Pulse" to "Trigger haptic pulse"
            )

            testActions.forEach { (label, cmd) ->
                Surface(
                    onClick = { onTestTool(cmd) },
                    shape = RoundedCornerShape(8.dp),
                    color = JarvisSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderCyan),
                    modifier = Modifier.testTag("invoke_tool_${label.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Tool Invocation History
        Text(
            text = "DISPATCHED TOOL LOGS",
            color = JarvisTextSecondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        if (tools.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(JarvisSurfaceDark, RoundedCornerShape(10.dp))
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tools invoked yet. Speak or tap protocol tools above.",
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tools.reversed()) { tool ->
                    ToolCallCard(tool = tool, timeStr = timeFormat.format(Date(tool.timestamp)))
                }
            }
        }
    }
}

@Composable
fun TelemetryItem(label: String, value: String) {
    Column {
        Text(text = label, color = JarvisTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = JarvisTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ToolCallCard(tool: ToolCallInfo, timeStr: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.name,
                    color = JarvisGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (tool.status) {
                        ToolStatus.SUCCESS -> JarvisEmerald
                        ToolStatus.RUNNING -> JarvisCyan
                        ToolStatus.FAILED -> JarvisCrimson
                    }
                    val icon = when (tool.status) {
                        ToolStatus.SUCCESS -> Icons.Default.CheckCircle
                        ToolStatus.RUNNING -> Icons.Default.HourglassTop
                        ToolStatus.FAILED -> Icons.Default.Warning
                    }
                    Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tool.status.name,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "ID: ${tool.callId} | $timeStr",
                color = JarvisTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            if (tool.args.isNotEmpty()) {
                Text(
                    text = "Args: ${tool.args}",
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            if (tool.result != null) {
                Text(
                    text = "Response: ${tool.result}",
                    color = JarvisCyanGlow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}
