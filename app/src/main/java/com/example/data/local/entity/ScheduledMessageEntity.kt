package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val threadId: String,
    val recipientAddress: String,
    val recipientName: String?,
    val body: String,
    val scheduledTimestamp: Long,
    val simSlot: Int = 0,
    val status: String = STATUS_PENDING, // PENDING, SENT, FAILED, CANCELLED
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
