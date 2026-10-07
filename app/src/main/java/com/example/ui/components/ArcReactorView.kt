package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantState
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
    state: AssistantState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_rotations")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when (state) {
        AssistantState.SPEAKING -> JarvisCyanGlow
        AssistantState.LISTENING -> JarvisCyan
        AssistantState.PROCESSING, AssistantState.CALLING_TOOLS -> JarvisGold
        AssistantState.INTERRUPTED -> JarvisCrimson
        AssistantState.IDLE -> JarvisCyan.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("arc_reactor_core"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f - 12.dp.toPx()
            val dynamicScale = (1f + amplitude * 0.25f) * pulsePhase

            // Outer Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.25f + amplitude * 0.35f),
                        coreColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.15f * dynamicScale
                ),
                radius = baseRadius * 1.15f * dynamicScale,
                center = center
            )

            // Outer Rim
            drawCircle(
                color = coreColor.copy(alpha = 0.4f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Outer Segmented Cog / Tick Marks (12 notches)
            rotate(degrees = outerRotation, pivot = center) {
                val tickCount = 12
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI.toFloat() / 180f)
                    val startR = baseRadius - 10.dp.toPx()
                    val endR = baseRadius - 1.dp.toPx()
                    val start = Offset(center.x + startR * cos(angle), center.y + startR * sin(angle))
                    val end = Offset(center.x + endR * cos(angle), center.y + endR * sin(angle))
                    drawLine(
                        color = coreColor.copy(alpha = 0.8f),
                        start = start,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Intermediate dashed ring
                drawCircle(
                    color = coreColor.copy(alpha = 0.5f),
                    radius = baseRadius - 16.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Middle Counter-rotating Chamber with 3 Arc Segments
            rotate(degrees = innerRotation, pivot = center) {
                val midRadius = baseRadius - 32.dp.toPx()
                for (i in 0 until 3) {
                    val startAngle = i * 120f + 15f
                    drawArc(
                        color = coreColor,
                        startAngle = startAngle,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 6 Power Nodes
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (Math.PI.toFloat() / 180f)
                    val nodeR = baseRadius - 42.dp.toPx()
                    val nodePos = Offset(center.x + nodeR * cos(angle), center.y + nodeR * sin(angle))
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = nodePos
                    )
                }
            }

            // Inner Core Chamber Ring
            val innerRingRadius = baseRadius - 56.dp.toPx()
            drawCircle(
                color = coreColor,
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Central Glowing Heart
            val heartRadius = (innerRingRadius - 8.dp.toPx()) * (0.85f + amplitude * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        coreColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = heartRadius
                ),
                radius = heartRadius,
                center = center
            )

            // Inner Core Triangle Emblem
            val triSize = 22.dp.toPx() * (1f + amplitude * 0.2f)
            val path = Path().apply {
                moveTo(center.x, center.y - triSize)
                lineTo(center.x + triSize * 0.866f, center.y + triSize * 0.5f)
                lineTo(center.x - triSize * 0.866f, center.y + triSize * 0.5f)
                close()
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Overlay status text in center when idle/processing
        if (state == AssistantState.SPEAKING) {
            Text(
                text = "JARVIS",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        } else if (state == AssistantState.LISTENING) {
            Text(
                text = "MIC",
                color = JarvisCyanGlow,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
