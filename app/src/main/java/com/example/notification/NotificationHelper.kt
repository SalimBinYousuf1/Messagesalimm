package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import com.example.receiver.SmsStatusReceiver

object NotificationHelper {
    const val CHANNEL_MESSAGES = "salim_messages_channel"
    const val KEY_TEXT_REPLY = "key_text_reply"
    const val ACTION_REPLY = "com.example.salimmessages.ACTION_REPLY"
    const val ACTION_MARK_READ = "com.example.salimmessages.ACTION_MARK_READ"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming text and multimedia messages"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMessageNotification(
        context: Context,
        threadId: String,
        senderNumber: String,
        senderName: String?,
        messageBody: String,
        timestamp: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannels(context)

        val displayName = senderName ?: senderNumber

        // Publish dynamic shortcut for conversation priority on API 25+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = Uri.parse("sms:$senderNumber")
                    putExtra("thread_id", threadId)
                    putExtra("address", senderNumber)
                }
                val shortcut = ShortcutInfo.Builder(context, "convo_$threadId")
                    .setShortLabel(displayName)
                    .setLongLabel(displayName)
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .setLongLived(true)
                    .build()
                shortcutManager?.pushDynamicShortcut(shortcut)
            } catch (e: Exception) {
                // Ignore shortcut error
            }
        }

        // Tap intent to open conversation
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("sms:$senderNumber")
            putExtra("thread_id", threadId)
            putExtra("address", senderNumber)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            threadId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Inline Reply RemoteInput Action
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()
        val replyIntent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra("thread_id", threadId)
            putExtra("address", senderNumber)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.hashCode() + 1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        )
        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Mark as Read Action
        val markReadIntent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra("thread_id", threadId)
            putExtra("address", senderNumber)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.hashCode() + 2,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Mark as read",
            markReadPendingIntent
        ).build()

        // Person & MessagingStyle
        val senderPerson = Person.Builder()
            .setName(displayName)
            .setKey(senderNumber)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("Me").build())
            .setConversationTitle(if (senderName != null) displayName else null)
            .addMessage(
                NotificationCompat.MessagingStyle.Message(
                    messageBody,
                    timestamp,
                    senderPerson
                )
            )

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(messagingStyle)
            .setContentIntent(contentPendingIntent)
            .addAction(replyAction)
            .addAction(markReadAction)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setShortcutId("convo_$threadId")
        }

        notificationManager.notify(threadId.hashCode(), builder.build())
    }

    fun cancelNotification(context: Context, threadId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(threadId.hashCode())
    }
}
