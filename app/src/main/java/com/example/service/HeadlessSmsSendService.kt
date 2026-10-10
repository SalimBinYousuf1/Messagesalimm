package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (action == "android.telephony.action.RESPOND_VIA_MESSAGE") {
            val uri = intent.data
            val recipient = uri?.schemeSpecificPart
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (!recipient.isNullOrBlank() && !text.isNullOrBlank()) {
                try {
                    @Suppress("DEPRECATION")
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(recipient, null, text, null, null)
                    Log.d("HeadlessSmsSendService", "Sent respond via message to $recipient")
                } catch (e: Exception) {
                    Log.e("HeadlessSmsSendService", "Failed to send: ${e.message}")
                }
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
