package com.example

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.liquidGlassSurface
import com.example.ui.screens.BlockedNumbersScreen
import com.example.ui.screens.ConversationDetailScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.NewConversationScreen
import com.example.ui.screens.ScheduledQueueScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FrostAccentBlue
import com.example.ui.theme.FrostDarkCanvas
import com.example.ui.theme.FrostWhiteCanvas
import com.example.ui.theme.Headline
import com.example.ui.theme.LargeTitle
import com.example.ui.theme.SalimMessagesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshRoleAndSims()
        viewModel.loadContacts()
        viewModel.repository.registerTelephonyObserver()
    }

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.setUnlocked(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestInitialPermissions()
        handleIncomingIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
            val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()

            SalimMessagesTheme(themeMode = themeMode) {
                if (isAppLockEnabled && !isUnlocked) {
                    LockScreenOverlay(
                        onUnlockRequested = { launchBiometricAuth() }
                    )
                } else {
                    MainContent(viewModel = viewModel)
                }
            }
        }
    }

    fun launchBiometricAuth() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager != null && keyguardManager.isKeyguardSecure) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                "Unlock Salim Messages",
                "Authenticate using your fingerprint, face, or device PIN"
            )
            if (intent != null) {
                authLauncher.launch(intent)
                return
            }
        }
        // If device has no screen lock, unlock directly
        viewModel.setUnlocked(true)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRoleAndSims()
        viewModel.repository.registerTelephonyObserver()
    }

    private fun requestInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data = intent.data

        if (action == Intent.ACTION_SENDTO || action == Intent.ACTION_VIEW) {
            val address = data?.schemeSpecificPart
                ?: intent.getStringExtra("address")
                ?: intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)

            val threadId = intent.getStringExtra("thread_id") ?: address

            if (!address.isNullOrBlank()) {
                viewModel.openConversation(
                    threadId = threadId ?: address,
                    address = address,
                    name = null
                )
            }
        }
    }
}

@Composable
fun LockScreenOverlay(onUnlockRequested: () -> Unit) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) FrostDarkCanvas else FrostWhiteCanvas),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .liquidGlassSurface(RoundedCornerShape(32.dp), elevation = 12.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(FrostAccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = FrostAccentBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Salim Messages",
                    style = LargeTitle.copy(fontSize = 22.sp),
                    color = if (isDark) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "App is locked for your privacy.",
                    style = Headline.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                    color = if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUnlockRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = FrostAccentBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Messages", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) FrostDarkCanvas else FrostWhiteCanvas)
    ) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            // Adaptive Two-Pane Split Layout for Tablets and Foldables
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Conversation List
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                ) {
                    ConversationListScreen(viewModel = viewModel)
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
                )

                // Right Pane: Detail or Active Sub-screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (val screen = currentScreen) {
                        is ScreenDestination.ConversationDetail -> {
                            ConversationDetailScreen(
                                viewModel = viewModel,
                                threadId = screen.threadId,
                                address = screen.address,
                                name = screen.name
                            )
                        }
                        is ScreenDestination.NewConversation -> NewConversationScreen(viewModel = viewModel)
                        is ScreenDestination.Search -> SearchScreen(viewModel = viewModel)
                        is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                        is ScreenDestination.ScheduledQueue -> ScheduledQueueScreen(viewModel = viewModel)
                        is ScreenDestination.BlockedNumbers -> BlockedNumbersScreen(viewModel = viewModel)
                        is ScreenDestination.ConversationList -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a conversation to read and reply",
                                    color = if (isDark) Color(0x66FFFFFF) else Color(0x66000000)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Phone Single-Column Layout
            when (val screen = currentScreen) {
                is ScreenDestination.ConversationList -> ConversationListScreen(viewModel = viewModel)
                is ScreenDestination.ConversationDetail -> {
                    ConversationDetailScreen(
                        viewModel = viewModel,
                        threadId = screen.threadId,
                        address = screen.address,
                        name = screen.name
                    )
                }
                is ScreenDestination.NewConversation -> NewConversationScreen(viewModel = viewModel)
                is ScreenDestination.Search -> SearchScreen(viewModel = viewModel)
                is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                is ScreenDestination.ScheduledQueue -> ScheduledQueueScreen(viewModel = viewModel)
                is ScreenDestination.BlockedNumbers -> BlockedNumbersScreen(viewModel = viewModel)
            }
        }
    }
}
