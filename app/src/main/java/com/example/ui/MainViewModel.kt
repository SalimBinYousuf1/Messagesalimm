package com.example.ui

import android.app.Activity
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimMessagesApp
import com.example.audio.AudioPlayer
import com.example.audio.VoiceRecorder
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.data.model.ContactEntry
import com.example.data.model.ConversationItem
import com.example.data.model.MessageItem
import com.example.data.telephony.SimCardInfo
import com.example.data.telephony.SimManager
import com.example.data.telephony.SmsSegmentation
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ConversationFilter {
    ALL,
    UNREAD,
    PINNED,
    ARCHIVED
}

sealed class ScreenDestination {
    object ConversationList : ScreenDestination()
    data class ConversationDetail(val threadId: String, val address: String, val name: String?) : ScreenDestination()
    object NewConversation : ScreenDestination()
    object Search : ScreenDestination()
    object Settings : ScreenDestination()
    object ScheduledQueue : ScreenDestination()
    object BlockedNumbers : ScreenDestination()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimMessagesApp
    val repository = app.repository
    private val simManager = SimManager(application)

    val audioPlayer = AudioPlayer()
    val voiceRecorder = VoiceRecorder(application)

    // Current Navigation Destination
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.ConversationList)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Navigation backstack
    private val backStack = mutableListOf<ScreenDestination>()

    // Default Role Status
    private val _isDefaultSmsApp = MutableStateFlow(repository.isDefaultSmsApp())
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    // Active SIM cards
    private val _activeSimCards = MutableStateFlow<List<SimCardInfo>>(emptyList())
    val activeSimCards: StateFlow<List<SimCardInfo>> = _activeSimCards.asStateFlow()

    // Selected SIM slot for compose
    private val _selectedSimSlot = MutableStateFlow(0)
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    // Filter Tab (All, Unread, Pinned, Archived)
    private val _filterTab = MutableStateFlow(ConversationFilter.ALL)
    val filterTab: StateFlow<ConversationFilter> = _filterTab.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Biometric App Lock
    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Search query & results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<MessageItem>>(emptyList())
    val searchResults: StateFlow<List<MessageItem>> = _searchResults.asStateFlow()

    // Contacts
    private val _contacts = MutableStateFlow<List<ContactEntry>>(emptyList())
    val contacts: StateFlow<List<ContactEntry>> = _contacts.asStateFlow()

    // All Conversations
    val allConversations: StateFlow<List<ConversationItem>> = repository.observeConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Conversations based on active tab
    val filteredConversations: StateFlow<List<ConversationItem>> = combine(allConversations, filterTab) { list, filter ->
        when (filter) {
            ConversationFilter.ALL -> list.filter { !it.isArchived }
            ConversationFilter.UNREAD -> list.filter { it.unreadCount > 0 && !it.isArchived }
            ConversationFilter.PINNED -> list.filter { it.isPinned && !it.isArchived }
            ConversationFilter.ARCHIVED -> list.filter { it.isArchived }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scheduled messages flow
    val scheduledMessages: StateFlow<List<ScheduledMessageEntity>> = repository.observeScheduledMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Blocked numbers flow
    val blockedNumbers: StateFlow<List<BlockedNumberEntity>> = repository.blockedNumbersManager.allBlocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshRoleAndSims()
        loadContacts()
    }

    fun refreshRoleAndSims() {
        _isDefaultSmsApp.value = repository.isDefaultSmsApp()
        _activeSimCards.value = simManager.getActiveSimCards()
    }

    fun setFilterTab(tab: ConversationFilter) {
        _filterTab.value = tab
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setSimSlot(slot: Int) {
        _selectedSimSlot.value = slot
    }

    fun setAppLockEnabled(enabled: Boolean) {
        _isAppLockEnabled.value = enabled
        if (!enabled) _isUnlocked.value = true
    }

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }

    fun openConversation(threadId: String, address: String, name: String?) {
        navigateTo(ScreenDestination.ConversationDetail(threadId, address, name))
        viewModelScope.launch {
            repository.markThreadRead(threadId)
        }
    }

    fun loadContacts(query: String = "") {
        viewModelScope.launch {
            _contacts.value = repository.queryContacts(query)
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _searchResults.value = repository.searchAll(query)
        }
    }

    fun sendMessage(threadId: String, address: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                threadId = threadId,
                recipientAddress = address,
                body = body,
                simSlot = _selectedSimSlot.value
            )
        }
    }

    fun sendImage(threadId: String, address: String, imageUri: android.net.Uri, caption: String = "") {
        viewModelScope.launch {
            repository.sendImageMessage(
                threadId = threadId,
                recipientAddress = address,
                imageUri = imageUri,
                caption = caption,
                simSlot = _selectedSimSlot.value
            )
        }
    }

    fun sendVoiceNote(threadId: String, address: String) {
        val result = voiceRecorder.stopRecording() ?: return
        viewModelScope.launch {
            repository.sendVoiceMessage(
                threadId = threadId,
                recipientAddress = address,
                audioFile = result.file,
                durationMs = result.durationMs,
                amplitudes = result.amplitudes
            )
        }
    }

    fun scheduleMessage(
        threadId: String,
        address: String,
        name: String?,
        body: String,
        scheduledTimestamp: Long
    ) {
        if (body.isBlank()) return
        viewModelScope.launch {
            repository.scheduleMessage(
                threadId = threadId,
                recipientAddress = address,
                recipientName = name,
                body = body,
                scheduledTimestamp = scheduledTimestamp,
                simSlot = _selectedSimSlot.value
            )
        }
    }

    fun cancelScheduled(id: Long) {
        viewModelScope.launch {
            repository.cancelScheduledMessage(id)
        }
    }

    fun togglePin(threadId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(threadId, currentPinned)
        }
    }

    fun toggleArchive(threadId: String, currentArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(threadId, currentArchived)
        }
    }

    fun toggleMute(threadId: String, currentMuted: Boolean) {
        viewModelScope.launch {
            repository.toggleMute(threadId, currentMuted)
        }
    }

    fun deleteConversation(threadId: String) {
        viewModelScope.launch {
            repository.deleteThread(threadId)
            if (_currentScreen.value is ScreenDestination.ConversationDetail) {
                navigateBack()
            }
        }
    }

    fun deleteMultipleConversations(threadIds: Set<String>) {
        viewModelScope.launch {
            threadIds.forEach { id ->
                repository.deleteThread(id)
            }
        }
    }

    fun markThreadRead(threadId: String) {
        viewModelScope.launch {
            repository.markThreadAsRead(threadId)
        }
    }

    fun markThreadUnread(threadId: String) {
        viewModelScope.launch {
            repository.markThreadAsUnread(threadId)
        }
    }

    fun markAllThreadsRead() {
        viewModelScope.launch {
            repository.markAllThreadsAsRead()
        }
    }

    fun deleteSingleMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteSingleMessage(messageId)
        }
    }

    fun clearAppCache(onFinished: (Long) -> Unit) {
        viewModelScope.launch {
            val bytes = repository.clearAppCache()
            onFinished(bytes)
        }
    }

    fun blockContact(address: String, name: String?) {
        viewModelScope.launch {
            repository.blockedNumbersManager.blockNumber(address, name)
        }
    }

    fun unblockContact(address: String) {
        viewModelScope.launch {
            repository.blockedNumbersManager.unblockNumber(address)
        }
    }

    fun toggleReaction(messageId: String, threadId: String, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId, threadId, emoji)
        }
    }

    fun exportBackup(onExported: (File) -> Unit) {
        viewModelScope.launch {
            val file = repository.exportBackupJson()
            onExported(file)
        }
    }

    /**
     * Triggers the OS default SMS app selection dialog via RoleManager (API 29+) or legacy intent.
     */
    fun requestDefaultSmsRole(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    activity.startActivityForResult(intent, 1001)
                }
            }
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
            }
            activity.startActivity(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        voiceRecorder.cancelRecording()
    }
}
