package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LocalMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalMessageDao {
    @Query("SELECT * FROM local_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<LocalMessageEntity>>

    @Query("SELECT * FROM local_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<LocalMessageEntity>>

    @Query("SELECT * FROM local_messages WHERE body LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<LocalMessageEntity>

    @Query("SELECT COUNT(*) FROM local_messages")
    suspend fun getCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM local_messages WHERE address = :address AND timestamp = :timestamp LIMIT 1)")
    suspend fun messageExists(address: String, timestamp: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: LocalMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<LocalMessageEntity>)

    @Update
    suspend fun update(message: LocalMessageEntity)

    @Query("UPDATE local_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE local_messages SET isRead = 1 WHERE threadId = :threadId")
    suspend fun markThreadRead(threadId: String)

    @Query("UPDATE local_messages SET isRead = :isRead WHERE threadId = :threadId")
    suspend fun setThreadReadStatus(threadId: String, isRead: Boolean)

    @Query("UPDATE local_messages SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM local_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM local_messages WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM local_messages WHERE threadId = :threadId")
    suspend fun deleteThread(threadId: String)
}
