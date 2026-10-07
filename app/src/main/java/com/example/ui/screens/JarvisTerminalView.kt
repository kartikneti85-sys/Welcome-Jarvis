package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConsoleLogEntry
import com.example.model.LogType
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JarvisTerminalView(
    logs: List<ConsoleLogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgDark)
            .padding(12.dp)
    ) {
        // Terminal Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = JarvisSurfaceVariant,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderCyan)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JARVIS CONSOLE // STDOUT STREAM",
                        color = JarvisCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(28.dp).testTag("clear_terminal_button")
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear logs",
                        tint = JarvisTextSecondary
                    )
                }
            }
        }

        // Terminal Log Output Window
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(JarvisSurfaceDark)
                .border(
                    1.dp,
                    JarvisBorderCyan,
                    RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "Awaiting terminal output...",
                    color = JarvisTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(logs) { index, entry ->
                        TerminalLogLine(
                            index = index + 1,
                            entry = entry,
                            timeStr = timeFormat.format(Date(entry.timestamp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalLogLine(
    index: Int,
    entry: ConsoleLogEntry,
    timeStr: String
) {
    val tagColor = when (entry.type) {
        LogType.USER -> Color.White
        LogType.JARVIS -> JarvisCyan
        LogType.TOOL -> JarvisGold
        LogType.STATUS -> JarvisCyanGlow
        LogType.SYSTEM -> JarvisTextSecondary
    }

    val contentColor = when (entry.type) {
        LogType.USER -> Color.White
        LogType.JARVIS -> JarvisCyanGlow
        LogType.TOOL -> JarvisGold
        LogType.STATUS -> if (entry.message.contains("INTERRUPTED")) JarvisCrimson else JarvisCyan
        LogType.SYSTEM -> JarvisTextPrimary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Line number
        Text(
            text = String.format("%03d ", index),
            color = JarvisTextSecondary.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.width(32.dp)
        )

        // Timestamp
        Text(
            text = "[$timeStr] ",
            color = JarvisTextSecondary.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )

        // Tag
        Text(
            text = "[${entry.tag}] ",
            color = tagColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        // Message
        Text(
            text = entry.message,
            color = contentColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
