package com.example.data.telephony

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.SmsStatusReceiver

object ScheduledMessageScheduler {

    fun scheduleExact(context: Context, scheduledId: Long, timestampMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = SmsStatusReceiver.ACTION_SCHEDULED_SEND_ALARM
            putExtra(SmsStatusReceiver.EXTRA_SCHEDULED_ID, scheduledId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduledId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timestampMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timestampMillis,
                    pendingIntent
                )
            }
            Log.d("ScheduledScheduler", "Alarm set for ID $scheduledId at $timestampMillis")
        } catch (e: Exception) {
            Log.e("ScheduledScheduler", "Error setting exact alarm: ${e.message}")
        }
    }

    fun cancel(context: Context, scheduledId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = SmsStatusReceiver.ACTION_SCHEDULED_SEND_ALARM
            putExtra(SmsStatusReceiver.EXTRA_SCHEDULED_ID, scheduledId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduledId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
