package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_messages")
data class LocalMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val threadId: String,
    val address: String,
    val senderName: String? = null,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: String = STATUS_DELIVERED, // SENDING, SENT, DELIVERED, FAILED
    val isRead: Boolean = true,
    val messageType: String = TYPE_SMS, // SMS, MMS, VOICE, IMAGE
    val mediaUri: String? = null,
    val audioAmplitudes: String? = null, // Comma-separated integers e.g. "20,45,80,60,30"
    val audioDurationMs: Int = 0,
    val simSlot: Int = 0
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
