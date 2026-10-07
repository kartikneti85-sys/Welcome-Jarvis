package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun JarvisSettingsDialog(
    currentPitch: Float,
    currentRate: Float,
    onDismiss: () -> Unit,
    onSaveVoiceConfig: (Float, Float) -> Unit,
    onSaveApiKey: (String) -> Unit
) {
    var apiKeyText by remember { mutableStateOf("") }
    var pitch by remember { mutableFloatStateOf(currentPitch) }
    var rate by remember { mutableFloatStateOf(currentRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JarvisSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(14.dp))
            .testTag("settings_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = JarvisCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEURAL PROTOCOL CONFIG",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Gemini API Uplink Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = JarvisGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GEMINI API UPLINK (OPTIONAL)",
                            color = JarvisGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Built-in Stark neural offline engine is active. Provide key to uplink with Gemini 3.5 Flash.",
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        placeholder = { Text("Paste AI Studio API Key...", color = JarvisTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderCyan,
                            focusedContainerColor = JarvisSurfaceVariant,
                            unfocusedContainerColor = JarvisSurfaceVariant
                        )
                    )
                }

                // Voice Modulation Sliders
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = JarvisCyanGlow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VOICE MODULATION (BRITISH SYNTH)",
                            color = JarvisCyanGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pitch
                    Text(
                        text = "Pitch: ${String.format("%.2f", pitch)}x",
                        color = JarvisTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyanGlow,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )

                    // Rate
                    Text(
                        text = "Speech Rate: ${String.format("%.2f", rate)}x",
                        color = JarvisTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = rate,
                        onValueChange = { rate = it },
                        valueRange = 0.7f..1.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyanGlow,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (apiKeyText.isNotBlank()) {
                        onSaveApiKey(apiKeyText)
                    }
                    onSaveVoiceConfig(pitch, rate)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("APPLY SETTINGS", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CLOSE", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}
