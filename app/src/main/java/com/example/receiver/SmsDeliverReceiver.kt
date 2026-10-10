package com.example.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.telephony.BlockedNumbersManager
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val originatingAddress = messages[0].originatingAddress ?: return
        val bodyBuilder = StringBuilder()
        var timestamp = System.currentTimeMillis()

        for (sms in messages) {
            bodyBuilder.append(sms.messageBody)
            timestamp = sms.timestampMillis
        }
        val fullBody = bodyBuilder.toString()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val blockedManager = BlockedNumbersManager(context, db.blockedNumberDao())

                // 1. Verify blocklist enforcement
                if (blockedManager.isNumberBlocked(originatingAddress)) {
                    Log.d("SmsDeliverReceiver", "Blocked incoming message from $originatingAddress")
                    return@launch
                }

                // 2. Write to system Telephony.Sms.Inbox if default SMS app
                try {
                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, originatingAddress)
                        put(Telephony.Sms.BODY, fullBody)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.READ, 0)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                } catch (e: Exception) {
                    Log.w("SmsDeliverReceiver", "Failed to insert into Telephony provider: ${e.message}")
                }

                // 3. Write to local database
                val threadId = originatingAddress // or calculated thread ID
                db.localMessageDao().insert(
                    LocalMessageEntity(
                        threadId = threadId,
                        address = originatingAddress,
                        body = fullBody,
                        timestamp = timestamp,
                        isOutgoing = false,
                        status = LocalMessageEntity.STATUS_DELIVERED,
                        isRead = false,
                        messageType = LocalMessageEntity.TYPE_SMS
                    )
                )

                // 4. Check if thread is muted or locked before posting notification
                val meta = db.conversationMetaDao().getMeta(threadId)
                if (meta == null || (!meta.isMuted && !meta.isLocked)) {
                    NotificationHelper.showMessageNotification(
                        context = context,
                        threadId = threadId,
                        senderNumber = originatingAddress,
                        senderName = null,
                        messageBody = fullBody,
                        timestamp = timestamp
                    )
                }
            } catch (e: Exception) {
                Log.e("SmsDeliverReceiver", "Error processing SMS: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
