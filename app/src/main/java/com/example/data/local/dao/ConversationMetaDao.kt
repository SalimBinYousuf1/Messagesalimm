package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConversationMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationMetaDao {
    @Query("SELECT * FROM conversation_meta")
    fun getAllMeta(): Flow<List<ConversationMetaEntity>>

    @Query("SELECT * FROM conversation_meta WHERE threadId = :threadId")
    suspend fun getMeta(threadId: String): ConversationMetaEntity?

    @Query("SELECT * FROM conversation_meta WHERE threadId = :threadId")
    fun observeMeta(threadId: String): Flow<ConversationMetaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(meta: ConversationMetaEntity)

    @Query("UPDATE conversation_meta SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun setPinned(threadId: String, isPinned: Boolean)

    @Query("UPDATE conversation_meta SET isMuted = :isMuted WHERE threadId = :threadId")
    suspend fun setMuted(threadId: String, isMuted: Boolean)

    @Query("UPDATE conversation_meta SET isArchived = :isArchived WHERE threadId = :threadId")
    suspend fun setArchived(threadId: String, isArchived: Boolean)

    @Query("UPDATE conversation_meta SET isLocked = :isLocked WHERE threadId = :threadId")
    suspend fun setLocked(threadId: String, isLocked: Boolean)

    @Query("UPDATE conversation_meta SET draftText = :draftText, draftTimestamp = :timestamp WHERE threadId = :threadId")
    suspend fun setDraft(threadId: String, draftText: String?, timestamp: Long)

    @Query("UPDATE conversation_meta SET selectedSimSlot = :simSlot WHERE threadId = :threadId")
    suspend fun setSelectedSim(threadId: String, simSlot: Int)
}
