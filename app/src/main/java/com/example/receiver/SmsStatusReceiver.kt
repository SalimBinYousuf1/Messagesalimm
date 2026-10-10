package com.example.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.RemoteInput
import com.example.data.local.AppDatabase
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                when (action) {
                    ACTION_SMS_SENT -> {
                        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                        val resultCode = resultCode
                        val isSuccess = resultCode == Activity.RESULT_OK
                        val newStatus = if (isSuccess) LocalMessageEntity.STATUS_SENT else LocalMessageEntity.STATUS_FAILED

                        if (messageId != -1L) {
                            db.localMessageDao().updateStatus(messageId, newStatus)
                            Log.d("SmsStatusReceiver", "Message $messageId sent status updated to $newStatus (code=$resultCode)")
                        }
                    }

                    ACTION_SMS_DELIVERED -> {
                        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                        if (messageId != -1L) {
                            db.localMessageDao().updateStatus(messageId, LocalMessageEntity.STATUS_DELIVERED)
                            Log.d("SmsStatusReceiver", "Message $messageId delivery confirmed")
                        }
                    }

                    NotificationHelper.ACTION_REPLY -> {
                        val threadId = intent.getStringExtra("thread_id") ?: return@launch
                        val address = intent.getStringExtra("address") ?: return@launch
                        val remoteInputResults = RemoteInput.getResultsFromIntent(intent)
                        val replyText = remoteInputResults?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString()

                        if (!replyText.isNullOrBlank()) {
                            // Send reply via SMS
                            try {
                                @Suppress("DEPRECATION")
                                val smsManager = SmsManager.getDefault()
                                smsManager.sendTextMessage(address, null, replyText, null, null)

                                // Insert to local DB
                                db.localMessageDao().insert(
                                    LocalMessageEntity(
                                        threadId = threadId,
                                        address = address,
                                        body = replyText,
                                        timestamp = System.currentTimeMillis(),
                                        isOutgoing = true,
                                        status = LocalMessageEntity.STATUS_SENT,
                                        isRead = true,
                                        messageType = LocalMessageEntity.TYPE_SMS
                                    )
                                )
                                // Clear notification
                                NotificationHelper.cancelNotification(context, threadId)
                            } catch (e: Exception) {
                                Log.e("SmsStatusReceiver", "Failed inline reply: ${e.message}")
                            }
                        }
                    }

                    NotificationHelper.ACTION_MARK_READ -> {
                        val threadId = intent.getStringExtra("thread_id") ?: return@launch
                        db.localMessageDao().markThreadRead(threadId)
                        NotificationHelper.cancelNotification(context, threadId)
                    }

                    ACTION_SCHEDULED_SEND_ALARM -> {
                        val scheduledId = intent.getLongExtra(EXTRA_SCHEDULED_ID, -1L)
                        if (scheduledId != -1L) {
                            val scheduledList = db.scheduledMessageDao().getAllPendingMessages()
                            val scheduled = scheduledList.find { it.id == scheduledId }
                            if (scheduled != null && scheduled.status == ScheduledMessageEntity.STATUS_PENDING) {
                                try {
                                    @Suppress("DEPRECATION")
                                    val smsManager = SmsManager.getDefault()
                                    smsManager.sendTextMessage(scheduled.recipientAddress, null, scheduled.body, null, null)

                                    db.scheduledMessageDao().updateStatus(scheduledId, ScheduledMessageEntity.STATUS_SENT)

                                    db.localMessageDao().insert(
                                        LocalMessageEntity(
                                            threadId = scheduled.threadId,
                                            address = scheduled.recipientAddress,
                                            body = scheduled.body,
                                            timestamp = System.currentTimeMillis(),
                                            isOutgoing = true,
                                            status = LocalMessageEntity.STATUS_SENT,
                                            isRead = true,
                                            messageType = LocalMessageEntity.TYPE_SMS
                                        )
                                    )
                                    Log.d("SmsStatusReceiver", "Scheduled message $scheduledId sent successfully")
                                } catch (e: Exception) {
                                    db.scheduledMessageDao().updateStatus(
                                        scheduledId,
                                        ScheduledMessageEntity.STATUS_FAILED,
                                        e.message
                                    )
                                    Log.e("SmsStatusReceiver", "Scheduled send failed: ${e.message}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsStatusReceiver", "Error handling status action $action: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.example.salimmessages.SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.example.salimmessages.SMS_DELIVERED"
        const val ACTION_SCHEDULED_SEND_ALARM = "com.example.salimmessages.SCHEDULED_SEND_ALARM"

        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_SCHEDULED_ID = "extra_scheduled_id"
    }
}
