package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.TelephonyRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SalimMessagesApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: TelephonyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = TelephonyRepository(this, database)

        // Setup notification channel
        NotificationHelper.createChannels(this)

        // Clean legacy demo seeds and synchronize real device messages
        CoroutineScope(Dispatchers.IO).launch {
            repository.cleanLegacySeedDataAndSync()
        }
    }
}
