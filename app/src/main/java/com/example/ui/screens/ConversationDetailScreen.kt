package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ConversationItem
import com.example.data.model.MessageItem
import com.example.data.telephony.SmsSegmentation
import com.example.ui.MainViewModel
import com.example.ui.components.AppleContactActionSheet
import com.example.ui.components.AppleMessageActionSheet
import com.example.ui.components.ReactionBar
import com.example.ui.components.SegmentCounterPill
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.appleHoldOrClick
import com.example.ui.components.getBubbleShape
import com.example.ui.components.liquidGlassSurface
import android.content.Intent
import com.example.ui.theme.Body
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDestructiveRed
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    viewModel: MainViewModel,
    threadId: String,
    address: String,
    name: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val messagesFlow = remember(threadId) { viewModel.repository.observeThreadMessages(threadId) }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeSims by viewModel.activeSimCards.collectAsStateWithLifecycle()
    val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageItem?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showContactSheet by remember { mutableStateOf(false) }

    val contactSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val messageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgCanvas = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImage(threadId, address, uri)
        }
    }

    val segmentResult = remember(inputText) {
        SmsSegmentation.calculate(inputText)
    }

    val isRecording by viewModel.voiceRecorder.isRecording.collectAsStateWithLifecycle()
    val recordDurationMs by viewModel.voiceRecorder.recordingDurationMs.collectAsStateWithLifecycle()
    val recordAmps by viewModel.voiceRecorder.amplitudes.collectAsStateWithLifecycle()

    val currentPlayingUri by viewModel.audioPlayer.currentPlayingUri.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.audioPlayer.playbackProgress.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val initials = remember(name, address) {
        val title = name ?: address
        if (title.contains(" ")) {
            title.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        } else {
            title.take(2).uppercase()
        }
    }

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
            // Apple Top Bar: Back Chevron, Centered Avatar & Contact Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgCanvas)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Chevron (< Messages)
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

                    // Centered Contact Monogram & Label (Clickable to show Contact Action Sheet)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showContactSheet = true }
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
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
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = name ?: address,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary,
                            maxLines = 1
                        )

                        Text(
                            text = "Text Message • SMS",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }

                    // Direct Call Button & More Menu
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${address.trim()}"))
                                    context.startActivity(dialIntent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Contact",
                                tint = FrostAccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = FrostAccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Schedule Message") },
                                onClick = {
                                    showMenu = false
                                    showScheduleDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Attach Photo / Media") },
                                onClick = {
                                    showMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                            )
                            if (activeSims.size > 1) {
                                DropdownMenuItem(
                                    text = { Text("Switch to ${if (selectedSimSlot == 0) "SIM 2" else "SIM 1"}") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setSimSlot(if (selectedSimSlot == 0) 1 else 0)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Block Contact") },
                                onClick = {
                                    showMenu = false
                                    viewModel.blockContact(address, name)
                                },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Conversation", color = FrostDestructiveRed) },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteConversation(threadId)
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FrostDestructiveRed) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
        }

            // Message Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AppleMessageBubble(
                        message = msg,
                        isLastMessage = (msg.id == messages.lastOrNull()?.id),
                        currentPlayingUri = currentPlayingUri,
                        playbackProgress = playbackProgress,
                        isPlaying = isPlaying,
                        onPlayAudio = { uri ->
                            viewModel.audioPlayer.play(uri, coroutineScope)
                        },
                        onImageClick = { uri ->
                            previewImageUri = uri
                        },
                        onLongPress = { selectedMessageForReaction = msg },
                        onRetrySend = {
                            viewModel.sendMessage(threadId, address, msg.body)
                        }
                    )
                }
            }

            // Apple Reaction Pill Popup
            AnimatedVisibility(
                visible = selectedMessageForReaction != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedMessageForReaction?.let { targetMsg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        color = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("❤️", "👍", "👎", "😂", "‼️", "❓").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            viewModel.toggleReaction(targetMsg.id, threadId, emoji)
                                            selectedMessageForReaction = null
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Apple iOS Compose Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = if (isDark) Color(0xFF121212) else Color(0xFFF7F7F8),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Segment Counter Indicator (only when text exists)
                    if (inputText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp, start = 44.dp, end = 44.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (segmentResult.isUcs2) "${segmentResult.remainingInSegment} left • ${segmentResult.segmentCount} UCS-2"
                                else "${segmentResult.remainingInSegment} left (${segmentResult.segmentCount})",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            if (activeSims.size > 1) {
                                Text(
                                    text = "SIM ${selectedSimSlot + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostAccentBlue
                                )
                            }
                        }
                    }

                    // Input Bar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular "+" Attachment Button
                        Box {
                            IconButton(
                                onClick = { showAttachMenu = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Attach",
                                    tint = if (isDark) Color.White else Color(0xFF636366),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showAttachMenu,
                                onDismissRequest = { showAttachMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Photo Library") },
                                    onClick = {
                                        showAttachMenu = false
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Schedule Message") },
                                    onClick = {
                                        showAttachMenu = false
                                        showScheduleDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Capsule Field or Voice Recording Waveform
                        if (isRecording) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color(0xFFFF3B30).copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FrostDestructiveRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${recordDurationMs / 1000}s",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostDestructiveRed
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                WaveformVisualizer(
                                    amplitudes = recordAmps,
                                    playbackProgress = 1f,
                                    isOutgoing = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF))
                                    .border(0.5.dp, if (isDark) Color(0xFF2C2C2E) else Color(0xFFD1D1D6), RoundedCornerShape(19.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Text Message",
                                        fontSize = 16.sp,
                                        color = textSecondary
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = Body.copy(
                                        fontSize = 16.sp,
                                        color = textPrimary
                                    ),
                                    cursorBrush = SolidColor(FrostAccentBlue),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Apple Circular Send / Mic Button
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val text = inputText
                                    inputText = ""
                                    viewModel.sendMessage(threadId, address, text)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(FrostAccentBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        } else if (isRecording) {
                            IconButton(
                                onClick = { viewModel.sendVoiceNote(threadId, address) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(FrostAccentBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.voiceRecorder.startRecording(coroutineScope) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record voice note",
                                    tint = if (isDark) Color.White else Color(0xFF636366),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Schedule Send Dialog
        if (showScheduleDialog) {
            ScheduleMessageDialog(
                onDismiss = { showScheduleDialog = false },
                onSchedule = { scheduledTime, bodyText ->
                    viewModel.scheduleMessage(threadId, address, name, bodyText, scheduledTime)
                    showScheduleDialog = false
                },
                initialBody = inputText
            )
        }

        // Fullscreen Image Preview Dialog
        previewImageUri?.let { imgUri ->
            Dialog(onDismissRequest = { previewImageUri = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.94f))
                        .clickable { previewImageUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = if (imgUri.startsWith("/")) File(imgUri) else imgUri,
                        contentDescription = "Full preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    IconButton(
                        onClick = { previewImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Apple Message Bubble Long-Press Action Sheet
        selectedMessageForReaction?.let { msg ->
            AppleMessageActionSheet(
                message = msg,
                sheetState = messageSheetState,
                onDismiss = { selectedMessageForReaction = null },
                onSelectEmoji = { emoji ->
                    viewModel.toggleReaction(msg.id, threadId, emoji)
                    selectedMessageForReaction = null
                },
                onDeleteMessage = {
                    msg.id.toLongOrNull()?.let { id ->
                        viewModel.deleteSingleMessage(id)
                    }
                    selectedMessageForReaction = null
                },
                onScheduleFollowUp = {
                    selectedMessageForReaction = null
                    showScheduleDialog = true
                }
            )
        }

        // Apple Contact Action Sheet (triggered by tapping or holding contact header)
        if (showContactSheet) {
            val contactItem = remember(threadId, address, name) {
                ConversationItem(
                    threadId = threadId,
                    address = address,
                    contactName = name,
                    snippet = messages.lastOrNull()?.body ?: "",
                    timestamp = messages.lastOrNull()?.timestamp ?: System.currentTimeMillis()
                )
            }
            AppleContactActionSheet(
                item = contactItem,
                sheetState = contactSheetState,
                onDismiss = { showContactSheet = false },
                onOpenChat = { showContactSheet = false },
                onTogglePin = { viewModel.togglePin(threadId, false) },
                onToggleArchive = { viewModel.toggleArchive(threadId, false) },
                onToggleMute = { viewModel.toggleMute(threadId, false) },
                onToggleRead = { viewModel.markThreadRead(threadId) },
                onBlockContact = { viewModel.blockContact(address, name) },
                onDeleteConversation = {
                    viewModel.deleteConversation(threadId)
                    showContactSheet = false
                }
            )
        }
    }
}

@Composable
fun AppleMessageBubble(
    message: MessageItem,
    isLastMessage: Boolean,
    currentPlayingUri: String?,
    playbackProgress: Float,
    isPlaying: Boolean,
    onPlayAudio: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onRetrySend: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isOutgoing = message.isOutgoing
    val bubbleShape = getBubbleShape(isOutgoing, message.isConsecutive)

    val bubbleBg = if (isOutgoing) {
        FrostAccentBlue
    } else {
        if (isDark) Color(0xFF262629) else Color(0xFFE9E9EB)
    }

    val textColor = if (isOutgoing) {
        Color.White
    } else {
        if (isDark) Color.White else Color.Black
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .appleHoldOrClick(
                    onHold = onLongPress,
                    onClick = {}
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            when {
                // Voice Message
                message.messageType == MessageItem.TYPE_VOICE -> {
                    val isCurrentTrack = (currentPlayingUri == message.mediaUri)
                    val progress = if (isCurrentTrack) playbackProgress else 0f
                    val isCurrentlyPlaying = (isCurrentTrack && isPlaying)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { message.mediaUri?.let { onPlayAudio(it) } },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isOutgoing) Color(0x33FFFFFF) else Color(0x1A000000))
                        ) {
                            Icon(
                                imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrentlyPlaying) "Pause" else "Play",
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            WaveformVisualizer(
                                amplitudes = message.audioAmplitudes,
                                playbackProgress = progress,
                                isOutgoing = isOutgoing
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${message.audioDurationMs / 1000}s voice note",
                                fontSize = 11.sp,
                                color = if (isOutgoing) Color(0xCCFFFFFF) else Color(0x998E8E93)
                            )
                        }
                    }
                }

                // Image / MMS Photo Message with real Coil rendering
                message.messageType == MessageItem.TYPE_IMAGE || (message.mediaUri != null && message.messageType != MessageItem.TYPE_VOICE) -> {
                    Column {
                        val mediaUri = message.mediaUri ?: ""
                        val imageModel = if (mediaUri.startsWith("/")) File(mediaUri) else mediaUri

                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Photo attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(mediaUri) }
                        )

                        if (message.body.isNotBlank() && message.body != "Photo attachment") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.body,
                                fontSize = 15.sp,
                                color = textColor
                            )
                        }
                    }
                }

                // Standard Text Message
                else -> {
                    Text(
                        text = message.body,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = textColor
                    )
                }
            }
        }

        // Apple Reaction Pill Bar
        if (message.reactions.isNotEmpty()) {
            ReactionBar(
                reactions = message.reactions,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Apple Status Label: "Delivered" under latest sent message only
        if (isOutgoing && isLastMessage) {
            val statusLabel = when (message.status) {
                MessageItem.STATUS_SENDING -> "Sending..."
                MessageItem.STATUS_SENT -> "Sent"
                MessageItem.STATUS_DELIVERED -> "Delivered"
                MessageItem.STATUS_FAILED -> "Not Delivered • Tap to retry"
                else -> "Delivered"
            }
            val statusColor = if (message.status == MessageItem.STATUS_FAILED) FrostDestructiveRed else Color(0xFF8E8E93)

            Text(
                text = statusLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = statusColor,
                modifier = Modifier
                    .padding(top = 3.dp, end = 4.dp)
                    .clickable(enabled = message.status == MessageItem.STATUS_FAILED) {
                        onRetrySend()
                    }
            )
        }
    }
}

@Composable
fun ScheduleMessageDialog(
    onDismiss: () -> Unit,
    onSchedule: (Long, String) -> Unit,
    initialBody: String
) {
    var body by remember { mutableStateOf(initialBody) }
    var calendar by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.MINUTE, 30) })
    }
    val context = LocalContext.current

    val formattedTime = remember(calendar.timeInMillis) {
        SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(calendar.timeInMillis))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Message", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text("Select date and time to send via telephony:", fontSize = 13.sp, color = Color(0xFF8E8E93))
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                calendar.set(Calendar.YEAR, y)
                                calendar.set(Calendar.MONTH, m)
                                calendar.set(Calendar.DAY_OF_MONTH, d)
                                TimePickerDialog(
                                    context,
                                    { _, hour, min ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, min)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(formattedTime)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Message text:", fontSize = 13.sp, color = Color(0xFF8E8E93))
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1A808080))
                        .padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSchedule(calendar.timeInMillis, body) },
                enabled = body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FrostAccentBlue)
            }
        }
    )
}
