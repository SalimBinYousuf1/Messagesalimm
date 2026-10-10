package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledTimestamp ASC")
    fun getAllScheduled(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE status = 'PENDING' AND scheduledTimestamp <= :currentTime")
    suspend fun getPendingDueMessages(currentTime: Long): List<ScheduledMessageEntity>

    @Query("SELECT * FROM scheduled_messages WHERE status = 'PENDING' ORDER BY scheduledTimestamp ASC")
    suspend fun getAllPendingMessages(): List<ScheduledMessageEntity>

    @Query("SELECT * FROM scheduled_messages WHERE threadId = :threadId AND status = 'PENDING'")
    fun observePendingForThread(threadId: String): Flow<List<ScheduledMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ScheduledMessageEntity): Long

    @Update
    suspend fun update(message: ScheduledMessageEntity)

    @Query("UPDATE scheduled_messages SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, errorMessage: String? = null)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteById(id: Long)
}
