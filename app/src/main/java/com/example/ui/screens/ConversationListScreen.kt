package com.example.ui.screens

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.components.liquidGlassSurface
import com.example.ui.components.springPressScale
import com.example.ui.theme.Body
import com.example.ui.theme.Callout
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostDarkGlassFill
import com.example.ui.theme.FrostDarkSurface
import com.example.ui.theme.FrostDestructiveRed
import com.example.ui.theme.FrostLightGlassFill
import com.example.ui.theme.FrostLightSurface
import com.example.ui.theme.FrostWhiteCanvas
import com.example.ui.theme.Headline
import com.example.ui.theme.LargeTitle
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
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsStateWithLifecycle()
    val activeFilter by viewModel.filterTab.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var threadToDelete by remember { mutableStateOf<ConversationItem?>(null) }

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
            // Apple Liquid Glass Frost Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .liquidGlassSurface(RoundedCornerShape(26.dp), elevation = 6.dp),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Messages",
                            style = LargeTitle,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Settings) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (isDark) Color.White else Color.Black
                                )
                            }
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenDestination.NewConversation) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FrostAccentBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Compose",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Glass Search Bar Tap Trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0x26FFFFFF) else Color(0x14000000))
                            .clickable { viewModel.navigateTo(ScreenDestination.Search) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isDark) Color(0x99FFFFFF) else Color(0x99000000),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search",
                            style = Callout,
                            color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Filter Tabs: All, Unread, Pinned, Archived
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConversationFilter.values().forEach { filter ->
                            val isSelected = activeFilter == filter
                            val pillBg = if (isSelected) FrostAccentBlue else (if (isDark) Color(0x1FFFFFFF) else Color(0x0F000000))
                            val pillText = if (isSelected) Color.White else (if (isDark) Color(0x99FFFFFF) else Color(0x99000000))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pillBg)
                                    .clickable { viewModel.setFilterTab(filter) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = Caption.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                    color = pillText
                                )
                            }
                        }
                    }
                }
            }

            // Default SMS App Banner if role is not held
            if (!isDefaultSmsApp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0x33FF9500) else Color(0x1AFF9500))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default SMS App",
                                style = Headline.copy(fontSize = 15.sp),
                                color = if (isDark) Color(0xFFFFB340) else Color(0xFFC76A00)
                            )
                            Text(
                                text = "Set as default to send SMS & MMS through device telephony.",
                                style = Caption,
                                color = if (isDark) Color(0xCCFFFFFF) else Color(0x99000000)
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
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Set", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Conversation Items List
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (activeFilter == ConversationFilter.ALL) "No Messages" else "No ${activeFilter.name.lowercase()} messages",
                            style = Headline,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the compose button above to start a conversation.",
                            style = Callout,
                            color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(conversations, key = { it.threadId }) { item ->
                        ConversationRowItem(
                            item = item,
                            onClick = {
                                viewModel.openConversation(item.threadId, item.address, item.contactName)
                            },
                            onTogglePin = { viewModel.togglePin(item.threadId, item.isPinned) },
                            onToggleArchive = { viewModel.toggleArchive(item.threadId, item.isArchived) },
                            onToggleMute = { viewModel.toggleMute(item.threadId, item.isMuted) },
                            onDelete = { threadToDelete = item }
                        )
                    }
                }
            }
        }

        // Delete Named Target Confirmation Dialog (Part 4)
        threadToDelete?.let { target ->
            val targetName = target.contactName ?: target.address
            AlertDialog(
                onDismissRequest = { threadToDelete = null },
                title = { Text("Delete Conversation") },
                text = { Text("Delete conversation with $targetName? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteConversation(target.threadId)
                            threadToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { threadToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ConversationRowItem(
    item: ConversationItem,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showMenu by remember { mutableStateOf(false) }

    val formattedDate = remember(item.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - item.timestamp
        if (diff < 24 * 60 * 60 * 1000L) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(item.timestamp))
        } else {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(item.timestamp))
        }
    }

    val initials = remember(item.contactName, item.address) {
        val name = item.contactName ?: item.address
        if (name.contains(" ")) {
            name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        } else {
            name.take(2).uppercase()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .springPressScale(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread blue dot indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (item.unreadCount > 0) FrostAccentBlue else Color.Transparent)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = Headline.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isDark) Color.White else Color(0xFF1C1C1E)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main conversation details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.contactName ?: item.address,
                        style = Headline.copy(fontSize = 17.sp, fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold),
                        color = if (isDark) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (item.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Muted",
                                tint = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = formattedDate,
                            style = Caption,
                            color = if (item.unreadCount > 0) FrostAccentBlue else (if (isDark) Color(0x80FFFFFF) else Color(0x80000000))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.snippet,
                        style = Body.copy(fontSize = 15.sp),
                        color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("•••", fontSize = 12.sp, color = if (isDark) Color(0x66FFFFFF) else Color(0x66000000))
                    }
                }
            }
        }

        // Context Menu for Pin, Mute, Archive, Delete
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (item.isPinned) "Unpin" else "Pin") },
                onClick = {
                    showMenu = false
                    onTogglePin()
                },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (item.isMuted) "Unmute" else "Mute") },
                onClick = {
                    showMenu = false
                    onToggleMute()
                },
                leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (item.isArchived) "Unarchive" else "Archive") },
                onClick = {
                    showMenu = false
                    onToggleArchive()
                },
                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = FrostDestructiveRed) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FrostDestructiveRed) }
            )
        }
    }
}
