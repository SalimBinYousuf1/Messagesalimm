package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_reactions")
data class ReactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val messageId: String,
    val threadId: String,
    val emoji: String,
    val sender: String = "me",
    val timestamp: Long = System.currentTimeMillis()
)
