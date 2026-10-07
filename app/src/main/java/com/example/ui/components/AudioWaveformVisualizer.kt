package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.AssistantState
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    amplitude: Float,
    state: AssistantState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val primaryColor = when (state) {
        AssistantState.SPEAKING -> JarvisCyanGlow
        AssistantState.LISTENING -> JarvisCyan
        AssistantState.PROCESSING, AssistantState.CALLING_TOOLS -> JarvisGold
        AssistantState.INTERRUPTED -> JarvisCrimson
        AssistantState.IDLE -> JarvisCyan.copy(alpha = 0.5f)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        val barCount = 32
        val spacing = 3.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
        val centerY = size.height / 2f
        val maxHeight = size.height * 0.9f

        val gradient = Brush.verticalGradient(
            colors = listOf(
                primaryColor,
                primaryColor.copy(alpha = 0.7f),
                primaryColor.copy(alpha = 0.2f)
            )
        )

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount.toFloat()
            // Bell curve dampening towards edges
            val bellCurve = sin(normalizedX * Math.PI.toFloat())

            // Wave pattern
            val harmonic1 = sin(normalizedX * 10f + wavePhase)
            val harmonic2 = sin(normalizedX * 18f - wavePhase * 1.5f)
            val combinedWave = abs(harmonic1 * 0.6f + harmonic2 * 0.4f)

            val dynamicFactor = if (state == AssistantState.IDLE) {
                0.12f + combinedWave * 0.08f
            } else {
                (amplitude * 0.85f + combinedWave * 0.25f) * bellCurve
            }

            val barHeight = (maxHeight * dynamicFactor.coerceIn(0.08f, 1.0f)).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
