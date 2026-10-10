package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.ui.MainViewModel
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.Body
import com.example.ui.theme.Callout
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostDestructiveRed
import com.example.ui.theme.FrostSuccessGreen
import com.example.ui.theme.FrostWhiteCanvas
import com.example.ui.theme.Headline
import com.example.ui.theme.Title
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduledQueueScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val scheduledMessages by viewModel.scheduledMessages.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

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
            // Glass Header
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
                        text = "Scheduled Messages",
                        style = Title.copy(fontSize = 20.sp),
                        color = if (isDark) Color.White else Color.Black
                    )
                }
            }

            if (scheduledMessages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Scheduled Messages",
                            style = Headline,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Schedule messages in any conversation using the clock button.",
                            style = Callout,
                            color = if (isDark) Color(0x80FFFFFF) else Color(0x80000000)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scheduledMessages, key = { it.id }) { item ->
                        val dateFormatted = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(item.scheduledTimestamp))
                        val statusColor = when (item.status) {
                            ScheduledMessageEntity.STATUS_PENDING -> Color(0xFFFF9500)
                            ScheduledMessageEntity.STATUS_SENT -> FrostSuccessGreen
                            ScheduledMessageEntity.STATUS_FAILED -> FrostDestructiveRed
                            else -> Color.Gray
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0A000000))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.recipientName ?: item.recipientAddress,
                                    style = Headline.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = item.status,
                                    style = Caption.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Scheduled: $dateFormatted",
                                style = Caption,
                                color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.body,
                                style = Body.copy(fontSize = 15.sp),
                                color = if (isDark) Color.White else Color.Black
                            )

                            if (item.status == ScheduledMessageEntity.STATUS_PENDING) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.cancelScheduled(item.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Cancel Send", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
