package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val normalizedNumber: String,
    val rawNumber: String,
    val contactName: String? = null,
    val blockedAt: Long = System.currentTimeMillis()
)
