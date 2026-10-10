package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.data.telephony.ScheduledMessageScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val pending = db.scheduledMessageDao().getAllPendingMessages()
                val now = System.currentTimeMillis()

                for (msg in pending) {
                    if (msg.scheduledTimestamp > now) {
                        ScheduledMessageScheduler.scheduleExact(context, msg.id, msg.scheduledTimestamp)
                        Log.d("BootReceiver", "Rescheduled message ${msg.id} for ${msg.scheduledTimestamp}")
                    } else {
                        // Mark as missed/failed or trigger immediately
                        ScheduledMessageScheduler.scheduleExact(context, msg.id, now + 1000)
                    }
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule on boot: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
