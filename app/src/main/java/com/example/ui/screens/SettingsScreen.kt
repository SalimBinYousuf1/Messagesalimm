package com.example.ui.screens

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.Body
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostSuccessGreen
import com.example.ui.theme.FrostWhiteCanvas
import com.example.ui.theme.Headline
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.Title

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val activeSims by viewModel.activeSimCards.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) FrostDarkCanvas else FrostWhiteCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Glass Floating Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .liquidGlassSurface(RoundedCornerShape(22.dp), elevation = 4.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        style = Title.copy(fontSize = 22.sp),
                        color = if (isDark) Color.White else Color.Black
                    )
                }
            }

            // Flat Grouped List Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Section 1: Telephony & Default Role
                SettingsSectionHeader("TELEPHONY & DEFAULT APP")

                SettingsRow(
                    icon = if (isDefaultSmsApp) Icons.Default.CheckCircle else Icons.Default.Warning,
                    iconColor = if (isDefaultSmsApp) FrostSuccessGreen else Color(0xFFFF9500),
                    title = "Default SMS App",
                    subtitle = if (isDefaultSmsApp) "Salim Messages is the active SMS & MMS default handler." else "Tap to grant default role. Required for incoming SMS & MMS broadcasts.",
                    action = {
                        if (!isDefaultSmsApp) {
                            Button(
                                onClick = {
                                    (context as? ComponentActivity)?.let {
                                        viewModel.requestDefaultSmsRole(it)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Set Default", fontSize = 12.sp)
                            }
                        }
                    }
                )

                // Active SIMs
                SettingsRow(
                    icon = Icons.Default.SimCard,
                    iconColor = FrostAccentBlue,
                    title = "Dual SIM Status",
                    subtitle = if (activeSims.isNotEmpty()) {
                        activeSims.joinToString(" • ") { "${it.displayName} (${it.carrierName})" }
                    } else {
                        "Single SIM active"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Management
                SettingsSectionHeader("CONVERSATION MANAGEMENT")

                SettingsRow(
                    icon = Icons.Default.Schedule,
                    iconColor = FrostAccentBlue,
                    title = "Scheduled Messages",
                    subtitle = "Manage queued messages scheduled with exact AlarmManager timing.",
                    onClick = { viewModel.navigateTo(ScreenDestination.ScheduledQueue) }
                )

                SettingsRow(
                    icon = Icons.Default.Block,
                    iconColor = Color(0xFFFF3B30),
                    title = "Blocked Numbers",
                    subtitle = "View and edit blocked phone numbers (enforced in database & BlockedNumberContract).",
                    onClick = { viewModel.navigateTo(ScreenDestination.BlockedNumbers) }
                )

                SettingsRow(
                    icon = Icons.Default.Download,
                    iconColor = FrostAccentBlue,
                    title = "Export & Backup Data",
                    subtitle = "Save a complete offline JSON backup of all conversations and messages.",
                    onClick = {
                        viewModel.exportBackup { file ->
                            try {
                                val shareIntent = viewModel.repository.createShareBackupIntent(file)
                                context.startActivity(Intent.createChooser(shareIntent, "Share or Save Messages Backup"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Exported to: ${file.name}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Appearance & Security
                SettingsSectionHeader("APPEARANCE & SECURITY")

                SettingsRow(
                    icon = Icons.Default.ColorLens,
                    iconColor = FrostAccentBlue,
                    title = "Theme",
                    subtitle = "Current: ${themeMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    action = {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ThemeMode.values().forEach { mode ->
                                if (mode != ThemeMode.AURORA || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val isSel = (themeMode == mode)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) FrostAccentBlue else (if (isDark) Color(0x26FFFFFF) else Color(0x14000000)))
                                            .clickable { viewModel.setThemeMode(mode) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = mode.name.take(3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else (if (isDark) Color(0x99FFFFFF) else Color(0x99000000))
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                SettingsRow(
                    icon = Icons.Default.Lock,
                    iconColor = FrostAccentBlue,
                    title = "Biometric App Lock",
                    subtitle = "Require biometric or system lock before opening conversations.",
                    action = {
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { viewModel.setAppLockEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FrostAccentBlue)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Honest Platform Transparency
                SettingsSectionHeader("PLATFORM CONTRACT DISCLOSURE")

                SettingsRow(
                    icon = Icons.Default.Info,
                    iconColor = if (isDark) Color(0x99FFFFFF) else Color(0x99000000),
                    title = "RCS Capability Notice",
                    subtitle = "Android does not currently offer a public API for third-party apps to send/receive RCS messages. RCS is restricted by the OS to Google's proprietary Messages app. Salim Messages delivers 100% real carrier SMS & MMS."
                )

                SettingsRow(
                    icon = Icons.Default.Info,
                    iconColor = if (isDark) Color(0x99FFFFFF) else Color(0x99000000),
                    title = "Carrier MMS Limits",
                    subtitle = "Attachments are compressed to a conservative ~300KB threshold to guarantee carrier MMSC compatibility across all networks."
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(text: String) {
    val isDark = isSystemInDarkTheme()
    Text(
        text = text,
        style = Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        color = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Headline.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Caption,
                color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
            )
        }

        if (action != null) {
            Spacer(modifier = Modifier.width(8.dp))
            action()
        }
    }
}
