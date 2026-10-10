package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BlockedNumberDao
import com.example.data.local.dao.ConversationMetaDao
import com.example.data.local.dao.LocalMessageDao
import com.example.data.local.dao.ReactionDao
import com.example.data.local.dao.ScheduledMessageDao
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.local.entity.ConversationMetaEntity
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.local.entity.ReactionEntity
import com.example.data.local.entity.ScheduledMessageEntity

@Database(
    entities = [
        ConversationMetaEntity::class,
        ScheduledMessageEntity::class,
        BlockedNumberEntity::class,
        ReactionEntity::class,
        LocalMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationMetaDao(): ConversationMetaDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun reactionDao(): ReactionDao
    abstract fun localMessageDao(): LocalMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salim_messages.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
