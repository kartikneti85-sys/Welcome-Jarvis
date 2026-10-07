package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.ui.JarvisUiState
import com.example.ui.components.ArcReactorView
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun JarvisHudView(
    uiState: JarvisUiState,
    onToggleListen: () -> Unit,
    onInterrupt: () -> Unit,
    onTextInputChanged: (String) -> Unit,
    onSubmitText: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onToggleFlashlight: () -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgDark)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Timer Banner (if scheduled)
        AnimatedVisibility(visible = uiState.activeTimerSeconds != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisGold, RoundedCornerShape(10.dp)),
                color = JarvisSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = JarvisGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.activeTimerLabel}: ${uiState.activeTimerSeconds}s remaining",
                            color = JarvisGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    IconButton(
                        onClick = onCancelTimer,
                        modifier = Modifier.size(28.dp).testTag("cancel_timer_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Timer", tint = JarvisGold)
                    }
                }
            }
        }

        // Central Arc Reactor Core
        Box(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            ArcReactorView(
                state = uiState.assistantState,
                amplitude = uiState.currentAmplitude,
                onClick = onToggleListen
            )
        }

        // Live HUD State Badge
        Surface(
            color = JarvisSurfaceVariant,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderCyan),
            modifier = Modifier.testTag("hud_state_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing Status Dot
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(
                            when (uiState.assistantState) {
                                AssistantState.SPEAKING -> JarvisCyanGlow
                                AssistantState.LISTENING -> JarvisCyan
                                AssistantState.PROCESSING, AssistantState.CALLING_TOOLS -> JarvisGold
                                AssistantState.INTERRUPTED -> JarvisCrimson
                                AssistantState.IDLE -> JarvisCyanDim
                            }
                        )
                )

                Text(
                    text = uiState.assistantState.hudTag,
                    color = when (uiState.assistantState) {
                        AssistantState.SPEAKING -> JarvisCyanGlow
                        AssistantState.LISTENING -> JarvisCyan
                        AssistantState.PROCESSING, AssistantState.CALLING_TOOLS -> JarvisGold
                        AssistantState.INTERRUPTED -> JarvisCrimson
                        AssistantState.IDLE -> JarvisTextSecondary
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Hands-Free vs Push-to-Talk (Media Detected) indicator
        Surface(
            color = if (uiState.diagnostics.isMediaPlaying) JarvisGold.copy(alpha = 0.15f) else JarvisEmerald.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, if (uiState.diagnostics.isMediaPlaying) JarvisGold else JarvisEmerald.copy(alpha = 0.6f)),
            modifier = Modifier.testTag("playback_mode_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.diagnostics.isMediaPlaying) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (uiState.diagnostics.isMediaPlaying) JarvisGold else JarvisEmerald,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.diagnostics.isMediaPlaying) "PUSH-TO-TALK ARMED (MEDIA ACTIVE)" else "HANDS-FREE VOICE ACTIVE",
                    color = if (uiState.diagnostics.isMediaPlaying) JarvisGold else JarvisEmerald,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Dynamic Frequency Waveform Visualizer
        AudioWaveformVisualizer(
            amplitude = uiState.currentAmplitude,
            state = uiState.assistantState,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Live Speech / Answering Transcription Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // User Voice Prompt Row
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "YOU (Voice): ",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (uiState.userSpokenTranscription.isNotBlank()) {
                            uiState.userSpokenTranscription
                        } else if (uiState.assistantState == AssistantState.LISTENING) {
                            "Listening to microphone input..."
                        } else {
                            "Ready. Speak or transmit below."
                        },
                        color = if (uiState.userSpokenTranscription.isNotBlank()) JarvisTextPrimary else JarvisTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Jarvis Response Row
                if (uiState.jarvisSpeechChunk.isNotBlank() || uiState.isModelSpeaking) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Jarvis: ",
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = uiState.jarvisSpeechChunk.ifBlank { "Synthesizing response..." },
                            color = JarvisCyanGlow,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Quick Protocol Chips
        val quickCommands = listOf(
            "System Diagnostics" to "Run diagnostics",
            "Torch On" to "Turn on flashlight",
            "Torch Off" to "Turn off flashlight",
            "Battery Status" to "What is my battery level?",
            "Set 1-min Timer" to "Set timer for 60 seconds",
            "Weather Report" to "Atmospheric weather in Malibu",
            "Record Intel" to "Note: Prototype initialized",
            "Calculate 48*125" to "Calculate 48 * 125"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickCommands.forEach { (label, command) ->
                Surface(
                    onClick = { onQuickCommand(command) },
                    shape = RoundedCornerShape(16.dp),
                    color = JarvisSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, JarvisBorderCyan),
                    modifier = Modifier.testTag("quick_chip_${label.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = label,
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Interruption / Barge-In Button (Prominently shown when model is speaking)
        AnimatedVisibility(
            visible = uiState.isModelSpeaking,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = onInterrupt,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barge_in_interrupt_button")
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Interrupt", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "■ [INTERRUPT] STOP JARVIS PLAYBACK",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Controls: Primary Mic Button & Terminal Text Transmitter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main Mic Button
            IconButton(
                onClick = onToggleListen,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.isListening) {
                            Brush.radialGradient(listOf(JarvisCyanGlow, JarvisCyanDim))
                        } else {
                            Brush.radialGradient(listOf(JarvisSurfaceVariant, JarvisSurfaceDark))
                        }
                    )
                    .border(1.5.dp, JarvisCyan, CircleShape)
                    .testTag("main_mic_button")
            ) {
                Icon(
                    imageVector = if (uiState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Voice Input Toggle",
                    tint = if (uiState.isListening) Color.Black else JarvisCyan,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Terminal Input Text Field
            OutlinedTextField(
                value = uiState.textInput,
                onValueChange = onTextInputChanged,
                placeholder = {
                    Text(
                        "Transmit command or tap mic...",
                        color = JarvisTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmitText() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorderCyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = JarvisSurfaceDark,
                    unfocusedContainerColor = JarvisSurfaceDark
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(
                        onClick = onSubmitText,
                        modifier = Modifier.testTag("send_command_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = JarvisCyan
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field")
            )

            // Direct Torch Toggle Button
            IconButton(
                onClick = onToggleFlashlight,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (uiState.isFlashlightOn) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                    .border(1.dp, if (uiState.isFlashlightOn) JarvisCyan else JarvisBorderCyan, RoundedCornerShape(12.dp))
                    .testTag("torch_toggle_button")
            ) {
                Icon(
                    imageVector = if (uiState.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Toggle Torch",
                    tint = if (uiState.isFlashlightOn) JarvisCyanGlow else JarvisTextSecondary
                )
            }
        }
    }
}
