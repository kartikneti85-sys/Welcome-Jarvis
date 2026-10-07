package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun JarvisGameStudioView(
    onTriggerVoiceCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tic-Tac-Toe", "Arc Reflex", "Generator")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Episode 15 Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = JarvisSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderCyan)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AUTONOMOUS APP & GAME GENERATOR",
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "PULSE OF AI // EPISODE 15 SPEC",
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                }

                Surface(
                    color = JarvisCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, JarvisCyan)
                ) {
                    Text(
                        text = "LIVE SANDBOX",
                        color = JarvisCyanGlow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = JarvisSurfaceDark,
            contentColor = JarvisCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = JarvisCyan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.testTag("game_studio_tab_$index")
                )
            }
        }

        // Active View
        when (selectedSubTab) {
            0 -> CyberTicTacToeGame(onVoiceComment = onTriggerVoiceCommand)
            1 -> ArcReflexChallenge(onVoiceComment = onTriggerVoiceCommand)
            2 -> AutonomousGeneratorStudio(onGenerate = onTriggerVoiceCommand)
        }
    }
}

/**
 * Game 1: Cyberpunk Tic-Tac-Toe vs JARVIS
 */
@Composable
fun CyberTicTacToeGame(onVoiceComment: (String) -> Unit) {
    val board = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var userWins by remember { mutableIntStateOf(0) }
    var jarvisWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("YOUR TURN, SIR (X)") }
    var isGameOver by remember { mutableStateOf(false) }

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            val (x, y, z) = line
            if (b[x].isNotEmpty() && b[x] == b[y] && b[y] == b[z]) return b[x]
        }
        return if (b.all { it.isNotEmpty() }) "TIE" else null
    }

    fun resetBoard() {
        for (i in 0..8) board[i] = ""
        statusText = "YOUR TURN, SIR (X)"
        isGameOver = false
    }

    fun makeJarvisMove() {
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isNotEmpty() && !isGameOver) {
            val choice = emptyIndices.random()
            board[choice] = "O"
            val winner = checkWinner(board)
            if (winner != null) {
                isGameOver = true
                if (winner == "O") {
                    jarvisWins++
                    statusText = "J.A.R.V.I.S. WINS! DEFENSIVE CONDUITS SECURE."
                    onVoiceComment("Jarvis won the Tic Tac Toe match, sir.")
                } else if (winner == "TIE") {
                    draws++
                    statusText = "TACTICAL STALEMATE (DRAW)"
                }
            } else {
                statusText = "YOUR TURN, SIR (X)"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Score Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceDark, RoundedCornerShape(8.dp))
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOU (X)", color = JarvisCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("$userWins", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("JARVIS (O)", color = JarvisGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("$jarvisWins", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DRAWS", color = JarvisTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("$draws", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // Status Banner
        Text(
            text = statusText,
            color = when {
                statusText.contains("YOU WIN") -> JarvisEmerald
                statusText.contains("JARVIS WINS") -> JarvisCrimson
                else -> JarvisCyanGlow
            },
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        // 3x3 Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val mark = board[index]
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisSurfaceDark)
                                .border(
                                    width = 1.2.dp,
                                    color = if (mark == "X") JarvisCyan else if (mark == "O") JarvisGold else JarvisBorderCyan,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = mark.isEmpty() && !isGameOver) {
                                    board[index] = "X"
                                    val winner = checkWinner(board)
                                    if (winner != null) {
                                        isGameOver = true
                                        if (winner == "X") {
                                            userWins++
                                            statusText = "EXCELLENT MOVE! YOU WIN, SIR!"
                                        } else if (winner == "TIE") {
                                            draws++
                                            statusText = "TACTICAL STALEMATE (DRAW)"
                                        }
                                    } else {
                                        statusText = "J.A.R.V.I.S. COMPUTING MOVE..."
                                        makeJarvisMove()
                                    }
                                }
                                .testTag("ttt_cell_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mark,
                                color = if (mark == "X") JarvisCyan else JarvisGold,
                                fontSize = 32.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { resetBoard() },
            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("reset_ttt_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("NEW ROUND", color = JarvisCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

/**
 * Game 2: Stark Arc Reflex Challenge
 */
@Composable
fun ArcReflexChallenge(onVoiceComment: (String) -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var activeNode by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(20) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            timeLeft = 20
            score = 0
            combo = 0
            while (timeLeft > 0 && isPlaying) {
                activeNode = Random.nextInt(0, 4)
                delay(900)
                timeLeft--
            }
            isPlaying = false
            activeNode = -1
            if (score > highScore) highScore = score
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Score & Chronometer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceDark, RoundedCornerShape(8.dp))
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SCORE", color = JarvisCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("$score", color = Color.White, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMBO", color = JarvisGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("${combo}x", color = JarvisGold, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TIME", color = JarvisTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("${timeLeft}s", color = if (timeLeft < 5) JarvisCrimson else Color.White, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = if (isPlaying) "TAP THE IGNITED POWER NODE!" else "TEST REFLEX VELOCITY AGAINST JARVIS",
            color = JarvisCyanGlow,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        // 4 Energy Node Matrix
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReflexNode(index = 0, isActive = activeNode == 0) {
                    if (isPlaying && activeNode == 0) {
                        score += (10 + combo * 2)
                        combo++
                        activeNode = -1
                    } else if (isPlaying) { combo = 0 }
                }
                ReflexNode(index = 1, isActive = activeNode == 1) {
                    if (isPlaying && activeNode == 1) {
                        score += (10 + combo * 2)
                        combo++
                        activeNode = -1
                    } else if (isPlaying) { combo = 0 }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReflexNode(index = 2, isActive = activeNode == 2) {
                    if (isPlaying && activeNode == 2) {
                        score += (10 + combo * 2)
                        combo++
                        activeNode = -1
                    } else if (isPlaying) { combo = 0 }
                }
                ReflexNode(index = 3, isActive = activeNode == 3) {
                    if (isPlaying && activeNode == 3) {
                        score += (10 + combo * 2)
                        combo++
                        activeNode = -1
                    } else if (isPlaying) { combo = 0 }
                }
            }
        }

        // Start / Abort Button
        Button(
            onClick = { isPlaying = !isPlaying },
            colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) JarvisCrimson else JarvisCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("toggle_reflex_game")
        ) {
            Icon(if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isPlaying) "ABORT DRILL" else "START REFLEX DRILL", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun ReflexNode(index: Int, isActive: Boolean, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(if (isActive) JarvisCyanGlow else JarvisSurfaceDark)
            .border(
                width = if (isActive) 3.dp else 1.5.dp,
                color = if (isActive) Color.White else JarvisBorderCyan,
                shape = CircleShape
            )
            .clickable(onClick = onTap)
            .testTag("reflex_node_$index"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Energy Node",
            tint = if (isActive) Color.Black else JarvisCyanDim,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Studio 3: Autonomous Code / App Generator
 */
@Composable
fun AutonomousGeneratorStudio(onGenerate: (String) -> Unit) {
    var promptInput by remember { mutableStateOf("") }
    var generatedPreview by remember { mutableStateOf("") }

    val sampleApps = listOf(
        "Cyberpunk Snake Game" to "Generate a Cyberpunk Snake Game in Python Tkinter",
        "Space Shooter Matrix" to "Generate an arcade Space Shooter game",
        "Password Vault Cipher" to "Generate a secure Password Vault and Cipher generator",
        "Pomodoro Timer Tool" to "Generate a dynamic Pomodoro chronometer utility",
        "Crypto Ticker Matrix" to "Generate a Crypto Telemetry and Portfolio viewer"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "AUTONOMOUS PROMPT SPECIFICATION",
            color = JarvisTextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            placeholder = { Text("Describe application or game to generate...", color = JarvisTextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth().testTag("app_generator_prompt"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisBorderCyan,
                focusedContainerColor = JarvisSurfaceDark,
                unfocusedContainerColor = JarvisSurfaceDark
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            onGenerate("Generate tool: $promptInput")
                            generatedPreview = """
# Generated Autonomous Tool: $promptInput
# Engine: Pulse of AI • Episode 15
import tkinter as tk

root = tk.Tk()
root.title("Autonomous Application: $promptInput")
root.geometry("450x600")
root.configure(bg="#060B14")

lbl = tk.Label(root, text="STARK AUTONOMOUS APP: $promptInput", font=("Consolas", 12, "bold"), fg="#00F0FF", bg="#060B14")
lbl.pack(pady=20)
# Subsystem code generated and compiled.
root.mainloop()
                            """.trimIndent()
                        }
                    },
                    modifier = Modifier.testTag("submit_generator_prompt")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Generate", tint = JarvisCyan)
                }
            }
        )

        // Sample Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sampleApps.forEach { (label, prompt) ->
                Surface(
                    onClick = {
                        promptInput = label
                        onGenerate("Generate tool: $prompt")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = JarvisSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, JarvisBorderCyan)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, color = JarvisCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Code Inspector Card
        if (generatedPreview.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GENERATED SOURCE CODE", color = JarvisGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("Python 3.x", color = JarvisTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Text(
                        text = generatedPreview,
                        color = JarvisCyanGlow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
