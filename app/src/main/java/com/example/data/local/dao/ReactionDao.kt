package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query("SELECT * FROM message_reactions WHERE threadId = :threadId")
    fun getReactionsForThread(threadId: String): Flow<List<ReactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: ReactionEntity)

    @Query("DELETE FROM message_reactions WHERE messageId = :messageId AND sender = :sender")
    suspend fun removeReaction(messageId: String, sender: String)

    @Query("DELETE FROM message_reactions WHERE threadId = :threadId")
    suspend fun clearReactionsForThread(threadId: String)

    @Query("DELETE FROM message_reactions WHERE messageId = :messageId")
    suspend fun clearReactionsForMessage(messageId: String)
}
