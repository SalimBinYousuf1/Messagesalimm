package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlocked(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_numbers WHERE normalizedNumber = :normalizedNumber LIMIT 1")
    suspend fun getBlocked(normalizedNumber: String): BlockedNumberEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE normalizedNumber = :normalizedNumber)")
    suspend fun isBlocked(normalizedNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedNumber: BlockedNumberEntity): Long

    @Query("DELETE FROM blocked_numbers WHERE normalizedNumber = :normalizedNumber")
    suspend fun deleteByNumber(normalizedNumber: String)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
