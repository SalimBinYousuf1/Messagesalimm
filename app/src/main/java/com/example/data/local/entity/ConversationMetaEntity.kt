package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_meta")
data class ConversationMetaEntity(
    @PrimaryKey
    val threadId: String,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val customNotificationTone: String? = null,
    val draftText: String? = null,
    val draftTimestamp: Long = 0L,
    val selectedSimSlot: Int = 0 // 0 for default/SIM 1, 1 for SIM 2
)
