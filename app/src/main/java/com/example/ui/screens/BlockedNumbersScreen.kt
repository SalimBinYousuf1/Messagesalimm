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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.Body
import com.example.ui.theme.Callout
import com.example.ui.theme.Caption
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostDestructiveRed
import com.example.ui.theme.FrostWhiteCanvas
import com.example.ui.theme.Headline
import com.example.ui.theme.Title

@Composable
fun BlockedNumbersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val blockedList by viewModel.blockedNumbers.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var inputNumber by remember { mutableStateOf("") }

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
                        text = "Blocked Numbers",
                        style = Title.copy(fontSize = 20.sp),
                        color = if (isDark) Color.White else Color.Black
                    )
                }
            }

            // Add Number to Block Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "BLOCK A PHONE NUMBER",
                    style = Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = if (isDark) Color(0x66FFFFFF) else Color(0x66000000)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x26FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputNumber.isEmpty()) {
                            Text(
                                text = "Enter phone number",
                                style = Body.copy(fontSize = 15.sp),
                                color = if (isDark) Color(0x66FFFFFF) else Color(0x66000000)
                            )
                        }
                        BasicTextField(
                            value = inputNumber,
                            onValueChange = { inputNumber = it },
                            textStyle = Body.copy(
                                fontSize = 15.sp,
                                color = if (isDark) Color.White else Color.Black
                            ),
                            cursorBrush = SolidColor(FrostAccentBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputNumber.isNotBlank()) {
                                viewModel.blockContact(inputNumber.trim(), null)
                                inputNumber = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostDestructiveRed),
                        shape = RoundedCornerShape(12.dp),
                        enabled = inputNumber.isNotBlank()
                    ) {
                        Text("Block", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Blocked Numbers List
            if (blockedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Blocked Numbers",
                            style = Headline,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incoming calls and messages from blocked numbers are rejected at telephony level.",
                            style = Callout,
                            color = if (isDark) Color(0x80FFFFFF) else Color(0x80000000)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(blockedList, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0A000000))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.contactName ?: item.rawNumber,
                                    style = Headline.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = item.normalizedNumber,
                                    style = Caption,
                                    color = if (isDark) Color(0x80FFFFFF) else Color(0x80000000)
                                )
                            }
                            TextButton(onClick = { viewModel.unblockContact(item.rawNumber) }) {
                                Text("Unblock", color = FrostAccentBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
