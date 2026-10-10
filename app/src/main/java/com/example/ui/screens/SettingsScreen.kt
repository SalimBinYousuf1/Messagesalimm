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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDestructiveRed
import com.example.ui.theme.FrostSuccessGreen
import com.example.ui.theme.ThemeMode

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

    var showMarkAllReadDialog by remember { mutableStateOf(false) }
    var deliveryReportsEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }

    val bgCanvas = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Apple Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.navigateBack() },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FrostAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Messages",
                        color = FrostAccentBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    modifier = Modifier.padding(end = 48.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)

            // Grouped Apple Settings Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Section 1: Telephony & Carrier Handler
                AppleSettingsHeader("TELEPHONY & DEFAULT APP")
                AppleGroupCard(cardBg = cardBg) {
                    AppleSettingRow(
                        icon = if (isDefaultSmsApp) Icons.Default.CheckCircle else Icons.Default.Warning,
                        iconBg = if (isDefaultSmsApp) FrostSuccessGreen else Color(0xFFFF9500),
                        title = "Default SMS App",
                        subtitle = if (isDefaultSmsApp) "Salim Messages active handler" else "Tap to set as default",
                        showDivider = true,
                        dividerColor = dividerColor,
                        action = {
                            if (!isDefaultSmsApp) {
                                Button(
                                    onClick = {
                                        (context as? ComponentActivity)?.let {
                                            viewModel.requestDefaultSmsRole(it)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Set", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    )

                    AppleSettingRow(
                        icon = Icons.Default.SimCard,
                        iconBg = FrostAccentBlue,
                        title = "Dual SIM Setup",
                        subtitle = if (activeSims.isNotEmpty()) {
                            activeSims.joinToString(" • ") { it.displayName }
                        } else {
                            "SIM 1 Active (Default slot)"
                        },
                        showDivider = false,
                        dividerColor = dividerColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Inbox Management & Tools
                AppleSettingsHeader("INBOX & MESSAGES")
                AppleGroupCard(cardBg = cardBg) {
                    AppleSettingRow(
                        icon = Icons.Default.DoneAll,
                        iconBg = Color(0xFF007AFF),
                        title = "Mark All as Read",
                        subtitle = "Clear all unread notification badges",
                        onClick = { showMarkAllReadDialog = true },
                        showDivider = true,
                        dividerColor = dividerColor
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Schedule,
                        iconBg = Color(0xFFFF9500),
                        title = "Scheduled Messages",
                        subtitle = "Manage AlarmManager exact queue",
                        onClick = { viewModel.navigateTo(ScreenDestination.ScheduledQueue) },
                        showDivider = true,
                        dividerColor = dividerColor,
                        showChevron = true
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Block,
                        iconBg = Color(0xFFFF3B30),
                        title = "Blocked Numbers",
                        subtitle = "Carrier and system-level blocklist",
                        onClick = { viewModel.navigateTo(ScreenDestination.BlockedNumbers) },
                        showDivider = true,
                        dividerColor = dividerColor,
                        showChevron = true
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Download,
                        iconBg = Color(0xFF5856D6),
                        title = "Export & Backup Data",
                        subtitle = "Encrypted offline JSON export",
                        onClick = {
                            viewModel.exportBackup { file ->
                                try {
                                    val shareIntent = viewModel.repository.createShareBackupIntent(file)
                                    context.startActivity(Intent.createChooser(shareIntent, "Share or Save Messages Backup"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Exported: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        showDivider = true,
                        dividerColor = dividerColor,
                        showChevron = true
                    )

                    AppleSettingRow(
                        icon = Icons.Default.CleaningServices,
                        iconBg = Color(0xFF64D2FF),
                        title = "Clear Cache & Temporary Files",
                        subtitle = "Purge cached audio recordings and images",
                        onClick = {
                            viewModel.clearAppCache { freedBytes ->
                                val kb = freedBytes / 1024
                                Toast.makeText(context, "Cleared $kb KB of temporary cache", Toast.LENGTH_SHORT).show()
                            }
                        },
                        showDivider = false,
                        dividerColor = dividerColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: Appearance & Security
                AppleSettingsHeader("APPEARANCE & PRIVACY")
                AppleGroupCard(cardBg = cardBg) {
                    AppleSettingRow(
                        icon = Icons.Default.ColorLens,
                        iconBg = Color(0xFFAF52DE),
                        title = "Theme",
                        subtitle = themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                        showDivider = true,
                        dividerColor = dividerColor,
                        action = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                                    val isSel = (themeMode == mode)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) FrostAccentBlue else (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)))
                                            .clickable { viewModel.setThemeMode(mode) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = mode.name.take(3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Lock,
                        iconBg = Color(0xFF34C759),
                        title = "Biometric App Lock",
                        subtitle = "Require fingerprint or PIN upon launch",
                        showDivider = true,
                        dividerColor = dividerColor,
                        action = {
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { viewModel.setAppLockEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FrostSuccessGreen
                                )
                            )
                        }
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Vibration,
                        iconBg = Color(0xFFFF9500),
                        title = "Haptic Touch Feedback",
                        subtitle = "Tactile vibration on message hold and actions",
                        showDivider = false,
                        dividerColor = dividerColor,
                        action = {
                            Switch(
                                checked = hapticFeedbackEnabled,
                                onCheckedChange = { hapticFeedbackEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FrostSuccessGreen
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 4: Telephony Compliance & Architecture
                AppleSettingsHeader("APP & SPECIFICATIONS")
                AppleGroupCard(cardBg = cardBg) {
                    AppleSettingRow(
                        icon = Icons.Default.Info,
                        iconBg = Color(0xFF8E8E93),
                        title = "Salim Messages",
                        subtitle = "Version 1.0.0 • com.salim.messages",
                        showDivider = true,
                        dividerColor = dividerColor
                    )

                    AppleSettingRow(
                        icon = Icons.Default.TextSnippet,
                        iconBg = Color(0xFF007AFF),
                        title = "3GPP Segmentation",
                        subtitle = "GSM-7 (160/153) & UCS-2 (70/67) auto-segmented",
                        showDivider = true,
                        dividerColor = dividerColor
                    )

                    AppleSettingRow(
                        icon = Icons.Default.Security,
                        iconBg = Color(0xFF34C759),
                        title = "Zero Cloud Tracking",
                        subtitle = "100% offline local Room DB. No external cloud relays.",
                        showDivider = false,
                        dividerColor = dividerColor
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Mark All As Read confirmation
        if (showMarkAllReadDialog) {
            AlertDialog(
                onDismissRequest = { showMarkAllReadDialog = false },
                title = { Text("Mark All as Read?", fontWeight = FontWeight.SemiBold) },
                text = { Text("All unread conversation badges will be cleared.", color = textSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markAllThreadsRead()
                            showMarkAllReadDialog = false
                            Toast.makeText(context, "All messages marked as read", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Mark Read")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMarkAllReadDialog = false }) {
                        Text("Cancel", color = FrostAccentBlue)
                    }
                }
            )
        }
    }
}

@Composable
fun AppleSettingsHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF8E8E93),
        modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
    )
}

@Composable
fun AppleGroupCard(
    cardBg: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
    ) {
        content()
    }
}

@Composable
fun AppleSettingRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    showDivider: Boolean,
    dividerColor: Color,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = textPrimary
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }

            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 0.5.dp,
                color = dividerColor
            )
        }
    }
}
