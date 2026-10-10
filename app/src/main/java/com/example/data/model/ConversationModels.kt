package com.example.data.model

import com.example.data.local.entity.ReactionEntity

data class MessageItem(
    val id: String,
    val threadId: String,
    val address: String,
    val senderName: String? = null,
    val body: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: String, // SENDING, SENT, DELIVERED, FAILED
    val isRead: Boolean,
    val messageType: String = TYPE_SMS, // SMS, MMS, VOICE, IMAGE
    val mediaUri: String? = null,
    val audioAmplitudes: List<Int> = emptyList(),
    val audioDurationMs: Int = 0,
    val simSlot: Int = 0,
    val reactions: List<ReactionEntity> = emptyList(),
    val isConsecutive: Boolean = false // Merged bubble flag for same sender
) {
    companion object {
        const val STATUS_SENDING = "SENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_DELIVERED = "DELIVERED"
        const val STATUS_FAILED = "FAILED"

        const val TYPE_SMS = "SMS"
        const val TYPE_MMS = "MMS"
        const val TYPE_VOICE = "VOICE"
        const val TYPE_IMAGE = "IMAGE"
    }
}

data class ConversationItem(
    val threadId: String,
    val address: String,
    val contactName: String?,
    val snippet: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val draftText: String? = null,
    val selectedSimSlot: Int = 0
)

data class ContactEntry(
    val id: String,
    val name: String,
    val number: String,
    val lookupKey: String? = null
)
