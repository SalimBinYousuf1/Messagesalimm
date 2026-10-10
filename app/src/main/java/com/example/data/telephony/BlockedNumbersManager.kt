package com.example.data.telephony

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.BlockedNumberContract
import android.util.Log
import com.example.data.local.dao.BlockedNumberDao
import com.example.data.local.entity.BlockedNumberEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BlockedNumbersManager(
    private val context: Context,
    private val blockedNumberDao: BlockedNumberDao
) {
    val allBlocked: Flow<List<BlockedNumberEntity>> = blockedNumberDao.getAllBlocked()

    /**
     * Checks if a number is blocked, testing normalized phone number against
     * both the local database and the Android system BlockedNumberContentProvider.
     */
    suspend fun isNumberBlocked(rawNumber: String): Boolean = withContext(Dispatchers.IO) {
        val normalized = PhoneNumberHelper.normalize(rawNumber)
        if (normalized.isBlank()) return@withContext false

        // 1. Check local Room database
        if (blockedNumberDao.isBlocked(normalized)) {
            return@withContext true
        }

        // 2. Check Android system BlockedNumberContentProvider (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                    val isBlockedSystem = BlockedNumberContract.isBlocked(context, rawNumber)
                    if (isBlockedSystem) {
                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                Log.w("BlockedNumbersManager", "Could not query BlockedNumberContract: ${e.message}")
            }
        }

        false
    }

    /**
     * Blocks a number by writing to both local Room DB and the system BlockedNumberContract.
     */
    suspend fun blockNumber(rawNumber: String, contactName: String? = null): Boolean = withContext(Dispatchers.IO) {
        val normalized = PhoneNumberHelper.normalize(rawNumber)
        if (normalized.isBlank()) return@withContext false

        // 1. Save to Room
        blockedNumberDao.insert(
            BlockedNumberEntity(
                normalizedNumber = normalized,
                rawNumber = rawNumber,
                contactName = contactName
            )
        )

        // 2. Save to System BlockedNumberContract
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                    val values = ContentValues().apply {
                        put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, rawNumber)
                        put(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER, normalized)
                    }
                    context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
                }
            } catch (e: Exception) {
                Log.w("BlockedNumbersManager", "Could not write to BlockedNumberContract: ${e.message}")
            }
        }
        true
    }

    /**
     * Unblocks a number.
     */
    suspend fun unblockNumber(rawNumber: String) = withContext(Dispatchers.IO) {
        val normalized = PhoneNumberHelper.normalize(rawNumber)
        blockedNumberDao.deleteByNumber(normalized)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                    val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
                    context.contentResolver.delete(
                        uri,
                        "${BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER} = ? OR ${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?",
                        arrayOf(normalized, rawNumber)
                    )
                }
            } catch (e: Exception) {
                Log.w("BlockedNumbersManager", "Could not delete from BlockedNumberContract: ${e.message}")
            }
        }
    }

    suspend fun unblockById(id: Long) = withContext(Dispatchers.IO) {
        blockedNumberDao.deleteById(id)
    }
}
