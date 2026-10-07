package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisTab
import com.example.ui.JarvisViewModel
import com.example.ui.screens.JarvisHudView
import com.example.ui.screens.JarvisIntelNotesView
import com.example.ui.screens.JarvisSettingsDialog
import com.example.ui.screens.JarvisTerminalView
import com.example.ui.screens.JarvisToolMatrixView
import com.example.ui.theme.JarvisBgDark
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisTheme {
                JarvisApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp(
    viewModel: JarvisViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes by viewModel.intelNotes.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    // Request permissions for mic and camera torch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBgDark),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = JarvisCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                        ) {
                            Text(
                                text = "STARK",
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "J.A.R.V.I.S.",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "// PROTOCOL",
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    // Battery indicator pill
                    Surface(
                        color = JarvisSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, JarvisBorderCyan),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.diagnostics.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = null,
                                tint = if (uiState.diagnostics.isCharging) JarvisGold else JarvisCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.diagnostics.batteryPercent}%",
                                color = JarvisTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Settings Icon
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = JarvisCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisSurfaceDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurfaceDark,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = JarvisBorderCyan,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
            ) {
                val tabs = listOf(
                    JarvisTab.HUD to ("HUD Core" to Icons.Default.GraphicEq),
                    JarvisTab.STUDIO to ("Game Studio" to Icons.Default.VideogameAsset),
                    JarvisTab.CONSOLE to ("Console" to Icons.Default.Terminal),
                    JarvisTab.TOOLS to ("Tool Matrix" to Icons.Default.Build),
                    JarvisTab.INTEL to ("Intel Vault" to Icons.Default.Description)
                )

                tabs.forEach { (tab, details) ->
                    val (label, icon) = details
                    val selected = uiState.currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.switchTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) JarvisCyanGlow else JarvisTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) JarvisCyan else JarvisTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = JarvisSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${label.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JarvisBgDark)
        ) {
            when (uiState.currentTab) {
                JarvisTab.HUD -> {
                    JarvisHudView(
                        uiState = uiState,
                        onToggleListen = { viewModel.toggleListening() },
                        onInterrupt = { viewModel.onUserInterrupt() },
                        onTextInputChanged = { viewModel.onTextInputChanged(it) },
                        onSubmitText = { viewModel.submitTextQuery() },
                        onQuickCommand = { viewModel.processQuickCommand(it) },
                        onToggleFlashlight = { viewModel.toggleFlashlightDirect() },
                        onCancelTimer = { viewModel.cancelActiveTimer() }
                    )
                }

                JarvisTab.STUDIO -> {
                    com.example.ui.screens.JarvisGameStudioView(
                        onTriggerVoiceCommand = { viewModel.processQuickCommand(it) }
                    )
                }

                JarvisTab.CONSOLE -> {
                    JarvisTerminalView(
                        logs = uiState.consoleLogs,
                        onClearLogs = { viewModel.clearConsoleLogs() }
                    )
                }

                JarvisTab.TOOLS -> {
                    JarvisToolMatrixView(
                        tools = uiState.activeToolCalls,
                        diagnostics = uiState.diagnostics,
                        onTestTool = { viewModel.processQuickCommand(it) },
                        onRefreshDiagnostics = { viewModel.refreshDiagnostics() }
                    )
                }

                JarvisTab.INTEL -> {
                    JarvisIntelNotesView(
                        notes = notes,
                        onAddNote = { viewModel.processQuickCommand(it) },
                        onDeleteNote = { viewModel.deleteNote(it) }
                    )
                }
            }

            if (showSettings) {
                JarvisSettingsDialog(
                    currentPitch = uiState.voicePitch,
                    currentRate = uiState.voiceRate,
                    onDismiss = { showSettings = false },
                    onSaveVoiceConfig = { pitch, rate -> viewModel.updateVoiceConfig(pitch, rate) },
                    onSaveApiKey = { key -> viewModel.updateCustomApiKey(key) }
                )
            }
        }
    }
}
