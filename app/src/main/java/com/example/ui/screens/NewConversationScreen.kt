package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContactEntry
import com.example.data.model.ConversationItem
import com.example.ui.MainViewModel
import com.example.ui.components.AppleContactActionSheet
import com.example.ui.components.appleHoldOrClick
import com.example.ui.theme.FrostAccentBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var sheetContact by remember { mutableStateOf<ContactEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgCanvas = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
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
            // Apple Top Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = "New Message",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )

                TextButton(
                    onClick = { viewModel.navigateBack() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = FrostAccentBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)

            // Apple "To:" Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "To:",
                    fontSize = 16.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.loadContacts(it)
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = textPrimary
                    ),
                    cursorBrush = SolidColor(FrostAccentBlue),
                    modifier = Modifier.weight(1f)
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            viewModel.loadContacts("")
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)

            // Direct start with phone number
            if (searchQuery.isNotBlank() && searchQuery.any { it.isDigit() }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.openConversation(
                                threadId = searchQuery.trim(),
                                address = searchQuery.trim(),
                                name = null
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FrostAccentBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = searchQuery.trim(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FrostAccentBlue
                        )
                        Text(
                            text = "Send SMS message",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
            }

            // Real Contacts List or Clean Empty State
            if (contacts.isEmpty()) {
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
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(FrostAccentBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "No Matching Contacts" else "Start a Message",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "No contact found for '$searchQuery'.\nTap the send arrow above to message directly."
                            else "Type any mobile number or contact name above to start messaging immediately.",
                            fontSize = 14.sp,
                            color = textSecondary,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contacts, key = { it.id }) { contact ->
                        val initials = remember(contact.name) {
                            if (contact.name.contains(" ")) {
                                contact.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                            } else {
                                contact.name.take(2).uppercase()
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .appleHoldOrClick(
                                    onHold = { sheetContact = contact },
                                    onClick = {
                                        viewModel.openConversation(
                                            threadId = contact.number,
                                            address = contact.number,
                                            name = contact.name
                                        )
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
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
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = contact.number,
                                        fontSize = 13.sp,
                                        color = textSecondary
                                    )
                                }

                                Text(
                                    text = "•••",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .clickable { sheetContact = contact }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                thickness = 0.5.dp,
                                color = dividerColor
                            )
                        }
                    }
                }
            }
        }

        // Apple Contact Action Sheet when holding a contact in New Conversation screen
        sheetContact?.let { target ->
            val conversationItem = remember(target) {
                ConversationItem(
                    threadId = target.number,
                    address = target.number,
                    contactName = target.name,
                    snippet = "",
                    timestamp = System.currentTimeMillis()
                )
            }

            AppleContactActionSheet(
                item = conversationItem,
                sheetState = sheetState,
                onDismiss = { sheetContact = null },
                onOpenChat = {
                    sheetContact = null
                    viewModel.openConversation(
                        threadId = target.number,
                        address = target.number,
                        name = target.name
                    )
                },
                onTogglePin = {},
                onToggleArchive = {},
                onToggleMute = {},
                onToggleRead = {},
                onBlockContact = {
                    viewModel.blockContact(target.number, target.name)
                    sheetContact = null
                },
                onDeleteConversation = {
                    sheetContact = null
                }
            )
        }
    }
}
