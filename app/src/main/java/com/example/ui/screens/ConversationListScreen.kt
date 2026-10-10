package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ConversationItem
import com.example.ui.ConversationFilter
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AppleContactActionSheet
import com.example.ui.components.appleHoldOrClick
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDestructiveRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.filteredConversations.collectAsStateWithLifecycle()
    val allConversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val activeFilter by viewModel.filterTab.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var threadToDelete by remember { mutableStateOf<ConversationItem?>(null) }
    var isSelectMode by remember { mutableStateOf(false) }
    val selectedThreadIds = remember { mutableStateListOf<String>() }

    var sheetConversation by remember { mutableStateOf<ConversationItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgCanvas = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val searchBarBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFE9E9EB)

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
            // Apple Top Navigation Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Top Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            isSelectMode = !isSelectMode
                            if (!isSelectMode) selectedThreadIds.clear()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isSelectMode) "Done" else "Edit",
                            color = FrostAccentBlue,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.Settings) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.NewConversation) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FrostAccentBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Compose",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Large Title: "Messages"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Messages",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        letterSpacing = 0.35.sp
                    )

                    if (isSelectMode) {
                        TextButton(
                            onClick = {
                                if (selectedThreadIds.size == conversations.size) {
                                    selectedThreadIds.clear()
                                } else {
                                    selectedThreadIds.clear()
                                    selectedThreadIds.addAll(conversations.map { it.threadId })
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedThreadIds.size == conversations.size) "Deselect All" else "Select All",
                                fontSize = 14.sp,
                                color = FrostAccentBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Native Apple iOS Search Bar Capsule
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(searchBarBg)
                        .clickable { viewModel.navigateTo(ScreenDestination.Search) }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Search",
                        fontSize = 16.sp,
                        color = textSecondary,
                        letterSpacing = (-0.2).sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Minimal Apple Filter Pills with badge counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ConversationFilter.values().forEach { filter ->
                        val isSelected = activeFilter == filter
                        val pillBg = if (isSelected) (if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)) else Color.Transparent
                        val pillTextColor = if (isSelected) textPrimary else textSecondary

                        val count = when (filter) {
                            ConversationFilter.ALL -> allConversations.size
                            ConversationFilter.UNREAD -> allConversations.count { it.unreadCount > 0 }
                            ConversationFilter.PINNED -> allConversations.count { it.isPinned }
                            ConversationFilter.ARCHIVED -> allConversations.count { it.isArchived }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(pillBg)
                                .clickable { viewModel.setFilterTab(filter) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = pillTextColor
                                )
                                if (count > 0 && filter != ConversationFilter.ALL) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = count.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) FrostAccentBlue else textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Minimalist Default SMS Banner if not default role
            if (!isDefaultSmsApp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default SMS App",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Set as default to receive SMS & MMS in real time.",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                (context as? androidx.activity.ComponentActivity)?.let {
                                    viewModel.requestDefaultSmsRole(it)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Set", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Conversation Items or Apple Empty State
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(FrostAccentBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (activeFilter == ConversationFilter.ALL) "No Messages" else "No ${activeFilter.name.lowercase()} messages",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You don't have any messages yet.\nTap below to start a new conversation.",
                            fontSize = 15.sp,
                            color = textSecondary,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.NewConversation) },
                            colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text("New Message", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = if (isSelectMode) 80.dp else 24.dp)
                ) {
                    items(conversations, key = { it.threadId }) { item ->
                        val isSelected = selectedThreadIds.contains(item.threadId)
                        AppleConversationRow(
                            item = item,
                            isSelectMode = isSelectMode,
                            isSelected = isSelected,
                            onToggleSelection = {
                                if (isSelected) selectedThreadIds.remove(item.threadId)
                                else selectedThreadIds.add(item.threadId)
                            },
                            onClick = {
                                if (isSelectMode) {
                                    if (isSelected) selectedThreadIds.remove(item.threadId)
                                    else selectedThreadIds.add(item.threadId)
                                } else {
                                    viewModel.openConversation(item.threadId, item.address, item.contactName)
                                }
                            },
                            onHold = {
                                if (!isSelectMode) {
                                    sheetConversation = item
                                }
                            }
                        )
                    }
                }
            }
        }

        // Multi-select Action Bar (Apple Bottom Floating Pill)
        AnimatedVisibility(
            visible = isSelectMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.95f))
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedThreadIds.forEach { id -> viewModel.markThreadRead(id) }
                            selectedThreadIds.clear()
                            isSelectMode = false
                        },
                        enabled = selectedThreadIds.isNotEmpty()
                    ) {
                        Text(
                            text = "Mark Read",
                            color = if (selectedThreadIds.isNotEmpty()) FrostAccentBlue else textSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (selectedThreadIds.isEmpty()) "Select Items" else "${selectedThreadIds.size} Selected",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    TextButton(
                        onClick = {
                            viewModel.deleteMultipleConversations(selectedThreadIds.toSet())
                            selectedThreadIds.clear()
                            isSelectMode = false
                        },
                        enabled = selectedThreadIds.isNotEmpty()
                    ) {
                        Text(
                            text = "Delete",
                            color = if (selectedThreadIds.isNotEmpty()) FrostDestructiveRed else textSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Apple Action Sheet (Triggered by holding contact for 1.2s or tapping •••)
        sheetConversation?.let { target ->
            AppleContactActionSheet(
                item = target,
                sheetState = sheetState,
                onDismiss = { sheetConversation = null },
                onOpenChat = {
                    sheetConversation = null
                    viewModel.openConversation(target.threadId, target.address, target.contactName)
                },
                onTogglePin = {
                    viewModel.togglePin(target.threadId, target.isPinned)
                },
                onToggleArchive = {
                    viewModel.toggleArchive(target.threadId, target.isArchived)
                },
                onToggleMute = {
                    viewModel.toggleMute(target.threadId, target.isMuted)
                },
                onToggleRead = {
                    if (target.unreadCount > 0) {
                        viewModel.markThreadRead(target.threadId)
                    } else {
                        viewModel.markThreadUnread(target.threadId)
                    }
                },
                onBlockContact = {
                    viewModel.blockContact(target.address, target.contactName)
                },
                onDeleteConversation = {
                    viewModel.deleteConversation(target.threadId)
                }
            )
        }

        // Delete Named Target Confirmation Dialog
        threadToDelete?.let { target ->
            val targetName = target.contactName ?: target.address
            AlertDialog(
                onDismissRequest = { threadToDelete = null },
                title = { Text("Delete Conversation", fontWeight = FontWeight.SemiBold) },
                text = { Text("Delete conversation with $targetName? This cannot be undone.", color = textSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteConversation(target.threadId)
                            threadToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { threadToDelete = null }) {
                        Text("Cancel", color = FrostAccentBlue)
                    }
                }
            )
        }
    }
}

@Composable
fun AppleConversationRow(
    item: ConversationItem,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit,
    onHold: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    val formattedDate = remember(item.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - item.timestamp
        if (diff < 24 * 60 * 60 * 1000L) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))
        } else if (diff < 7 * 24 * 60 * 60 * 1000L) {
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(item.timestamp))
        } else {
            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(item.timestamp))
        }
    }

    val initials = remember(item.contactName, item.address) {
        val name = item.contactName?.ifBlank { null } ?: item.address
        if (name.contains(" ")) {
            name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        } else {
            name.take(2).uppercase()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .appleHoldOrClick(
                onHold = onHold,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) FrostAccentBlue else textSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onToggleSelection)
                )
                Spacer(modifier = Modifier.width(10.dp))
            } else {
                // Apple Blue Unread Dot (10dp)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (item.unreadCount > 0) FrostAccentBlue else Color.Transparent)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Apple Circular Monogram Avatar (48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isDark) listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E))
                            else listOf(Color(0xFF8E8E93), Color(0xFF636366))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Conversation Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.contactName ?: item.address,
                        fontSize = 17.sp,
                        fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        if (item.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Muted",
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = formattedDate,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (item.unreadCount > 0) FrostAccentBlue else textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.snippet,
                        fontSize = 15.sp,
                        color = textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Quick Action Dots
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onHold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "•••",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                    }
                }
            }
        }

        // Apple Inset Divider (starts after avatar, 76dp inset)
        HorizontalDivider(
            modifier = Modifier.padding(start = if (isSelectMode) 104.dp else 76.dp),
            thickness = 0.5.dp,
            color = dividerColor
        )
    }
}
