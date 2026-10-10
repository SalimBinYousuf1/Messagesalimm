package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.data.model.MessageItem
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDestructiveRed

/**
 * Authentic Apple iOS Action Sheet for conversation & contact long-press (1.2s hold).
 * Theme-dependent: pure frosted white in Light mode, deep OLED / slate in Dark mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleContactActionSheet(
    item: ConversationItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleRead: () -> Unit,
    onBlockContact: () -> Unit,
    onDeleteConversation: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }

    val sheetBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)

    val cleanAddress = item.address.trim()
    val displayName = item.contactName?.ifBlank { null } ?: cleanAddress

    val initials = remember(displayName) {
        if (displayName.contains(" ")) {
            displayName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        } else {
            displayName.take(2).uppercase()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF48484A) else Color(0xFFD1D1D6))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // Header: Large Avatar and Contact Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isDark) listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E))
                                else listOf(Color(0xFF007AFF), Color(0xFF5856D6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cleanAddress,
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Capsule Row: Call, Message, Share, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleQuickActionButton(
                    icon = Icons.Default.Call,
                    label = "Call",
                    bgColor = cardBg,
                    tint = FrostAccentBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    onDismiss()
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanAddress"))
                        context.startActivity(dialIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                    }
                }

                AppleQuickActionButton(
                    icon = Icons.Default.ChatBubble,
                    label = "Chat",
                    bgColor = cardBg,
                    tint = FrostAccentBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    onDismiss()
                    onOpenChat()
                }

                AppleQuickActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    bgColor = cardBg,
                    tint = FrostAccentBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    onDismiss()
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Contact: $displayName\nPhone: $cleanAddress")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not share", Toast.LENGTH_SHORT).show()
                    }
                }

                AppleQuickActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    bgColor = cardBg,
                    tint = FrostAccentBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    onDismiss()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", cleanAddress))
                    Toast.makeText(context, "Copied $cleanAddress", Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group 1: Conversation State Actions (Pin, Read, Mute, Archive)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
            ) {
                AppleSheetActionRow(
                    icon = Icons.Default.PushPin,
                    title = if (item.isPinned) "Unpin Conversation" else "Pin to Top",
                    subtitle = if (item.isPinned) "Remove from top of inbox" else "Keep at top of list",
                    iconColor = FrostAccentBlue,
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    onTogglePin()
                }

                AppleSheetActionRow(
                    icon = if (item.unreadCount > 0) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                    title = if (item.unreadCount > 0) "Mark as Read" else "Mark as Unread",
                    subtitle = if (item.unreadCount > 0) "Clear unread badge" else "Mark for later",
                    iconColor = Color(0xFF34C759),
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    onToggleRead()
                }

                AppleSheetActionRow(
                    icon = if (item.isMuted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    title = if (item.isMuted) "Unmute Alerts" else "Mute Alerts",
                    subtitle = if (item.isMuted) "Receive notifications" else "Hide incoming alerts",
                    iconColor = Color(0xFFFF9500),
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    onToggleMute()
                }

                AppleSheetActionRow(
                    icon = Icons.Default.Archive,
                    title = if (item.isArchived) "Unarchive" else "Archive",
                    subtitle = if (item.isArchived) "Move back to inbox" else "Hide from active inbox",
                    iconColor = Color(0xFF5856D6),
                    showDivider = false,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    onToggleArchive()
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Group 2: Safety & Destructive (Block, Delete)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
            ) {
                AppleSheetActionRow(
                    icon = Icons.Default.Block,
                    title = "Block Contact",
                    subtitle = "Reject future messages from this number",
                    iconColor = Color(0xFFFF9500),
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    showBlockConfirm = true
                }

                AppleSheetActionRow(
                    icon = Icons.Default.Delete,
                    title = "Delete Conversation",
                    subtitle = "Remove all messages permanently",
                    iconColor = FrostDestructiveRed,
                    textColor = FrostDestructiveRed,
                    showDivider = false,
                    dividerColor = dividerColor
                ) {
                    showDeleteConfirm = true
                }
            }
        }
    }

    // Confirmation dialogs
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Conversation?", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to delete all messages with $displayName? This cannot be undone.", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDeleteConversation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = FrostAccentBlue)
                }
            }
        )
    }

    if (showBlockConfirm) {
        AlertDialog(
            onDismissRequest = { showBlockConfirm = false },
            title = { Text("Block $displayName?", fontWeight = FontWeight.SemiBold) },
            text = { Text("You will no longer receive SMS or MMS messages from this sender.", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirm = false
                        onDismiss()
                        onBlockContact()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirm = false }) {
                    Text("Cancel", color = FrostAccentBlue)
                }
            }
        )
    }
}

/**
 * Apple iOS Message Bubble Action Sheet for holding a message bubble.
 * Features Reactions bar (Love, Like, Dislike, Laugh, Emphasize, Question),
 * Copy, Share, Schedule Follow-up, Delete, and Message Info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleMessageActionSheet(
    message: MessageItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    onScheduleFollowUp: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF48484A) else Color(0xFFD1D1D6))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // Apple Top Reactions Capsule Bar
            Text(
                text = "Tapback Reaction",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emojis = listOf("❤️", "👍", "👎", "😂", "‼️", "❓")
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSelectEmoji(emoji)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Items Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
            ) {
                AppleSheetActionRow(
                    icon = Icons.Default.ContentCopy,
                    title = "Copy Text",
                    subtitle = "Copy message content to clipboard",
                    iconColor = FrostAccentBlue,
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Message Text", message.body))
                    Toast.makeText(context, "Copied message to clipboard", Toast.LENGTH_SHORT).show()
                }

                AppleSheetActionRow(
                    icon = Icons.Default.Forward,
                    title = "Share or Forward",
                    subtitle = "Send message to another contact or app",
                    iconColor = Color(0xFF34C759),
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    onDismiss()
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message.body)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Forward Message"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not forward", Toast.LENGTH_SHORT).show()
                    }
                }

                AppleSheetActionRow(
                    icon = Icons.Default.Info,
                    title = "Message Details",
                    subtitle = "${message.status} • SIM ${message.simSlot + 1} • ${java.text.SimpleDateFormat("h:mm a, MMM d", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))}",
                    iconColor = Color(0xFF5856D6),
                    showDivider = true,
                    dividerColor = dividerColor
                ) {
                    // Information display
                }

                AppleSheetActionRow(
                    icon = Icons.Default.Delete,
                    title = "Delete Message",
                    subtitle = "Remove this message from thread",
                    iconColor = FrostDestructiveRed,
                    textColor = FrostDestructiveRed,
                    showDivider = false,
                    dividerColor = dividerColor
                ) {
                    showDeleteConfirm = true
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Message?", fontWeight = FontWeight.SemiBold) },
            text = { Text("This message will be removed permanently.", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDeleteMessage()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = FrostAccentBlue)
                }
            }
        )
    }
}

@Composable
private fun AppleQuickActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color.White else Color.Black

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimary
        )
    }
}

@Composable
private fun AppleSheetActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    textColor: Color? = null,
    showDivider: Boolean,
    dividerColor: Color,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val titleColor = textColor ?: (if (isDark) Color.White else Color.Black)
    val textSecondary = Color(0xFF8E8E93)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 62.dp),
                thickness = 0.5.dp,
                color = dividerColor
            )
        }
    }
}
