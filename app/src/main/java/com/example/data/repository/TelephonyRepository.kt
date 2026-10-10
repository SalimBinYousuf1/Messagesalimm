package com.example.data.repository

import android.Manifest
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ConversationMetaEntity
import com.example.data.local.entity.LocalMessageEntity
import com.example.data.local.entity.ReactionEntity
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.data.model.ContactEntry
import com.example.data.model.ConversationItem
import com.example.data.model.MessageItem
import com.example.data.telephony.BlockedNumbersManager
import com.example.data.telephony.MediaAttachmentHelper
import com.example.data.telephony.PhoneNumberHelper
import com.example.data.telephony.ScheduledMessageScheduler
import com.example.receiver.SmsStatusReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelephonyRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    val blockedNumbersManager = BlockedNumbersManager(context, database.blockedNumberDao())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isObserverRegistered = false

    private val telephonyObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            coroutineScope.launch {
                syncSystemTelephonySms()
            }
        }
    }

    init {
        registerTelephonyObserver()
    }

    /**
     * Registers a live ContentObserver on Telephony.Sms.CONTENT_URI so that
     * any external or system SMS update is immediately reflected in the app.
     */
    fun registerTelephonyObserver() {
        if (isObserverRegistered) return
        val hasReadPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasReadPerm) {
            try {
                context.contentResolver.registerContentObserver(
                    Telephony.Sms.CONTENT_URI,
                    true,
                    telephonyObserver
                )
                isObserverRegistered = true
                coroutineScope.launch {
                    syncSystemTelephonySms()
                }
            } catch (e: Exception) {
                Log.w("TelephonyRepo", "Could not register ContentObserver: ${e.message}")
            }
        }
    }

    /**
     * Synchronizes real device SMS messages from the system content provider (Telephony.Sms)
     * into the Room local database so existing messages and system events appear seamlessly.
     */
    suspend fun syncSystemTelephonySms() = withContext(Dispatchers.IO) {
        val hasReadPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasReadPerm) return@withContext

        try {
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ,
                Telephony.Sms.TYPE
            )

            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 500"
            )

            cursor?.use { c ->
                val addressCol = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyCol = c.getColumnIndex(Telephony.Sms.BODY)
                val dateCol = c.getColumnIndex(Telephony.Sms.DATE)
                val readCol = c.getColumnIndex(Telephony.Sms.READ)
                val typeCol = c.getColumnIndex(Telephony.Sms.TYPE)

                val newMessages = mutableListOf<LocalMessageEntity>()

                while (c.moveToNext()) {
                    val address = c.getString(addressCol) ?: continue
                    val body = c.getString(bodyCol) ?: ""
                    val timestamp = c.getLong(dateCol)
                    val read = c.getInt(readCol) == 1
                    val type = c.getInt(typeCol)

                    val isOutgoing = type == Telephony.Sms.MESSAGE_TYPE_SENT
                    val status = if (isOutgoing) LocalMessageEntity.STATUS_DELIVERED else LocalMessageEntity.STATUS_DELIVERED

                    // Check if already stored to avoid duplication
                    if (!database.localMessageDao().messageExists(address, timestamp)) {
                        val senderName = queryContactName(address)
                        newMessages.add(
                            LocalMessageEntity(
                                threadId = address,
                                address = address,
                                senderName = senderName,
                                body = body,
                                timestamp = timestamp,
                                isOutgoing = isOutgoing,
                                status = status,
                                isRead = read,
                                messageType = LocalMessageEntity.TYPE_SMS
                            )
                        )
                    }
                }

                if (newMessages.isNotEmpty()) {
                    database.localMessageDao().insertAll(newMessages)
                    Log.d("TelephonyRepo", "Synced ${newMessages.size} messages from system Telephony provider.")
                }
            }
        } catch (e: Exception) {
            Log.e("TelephonyRepo", "Error syncing system telephony SMS: ${e.message}")
        }
    }

    /**
     * Checks whether Salim Messages is currently the default SMS app for the device.
     */
    fun isDefaultSmsApp(): Boolean {
        val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
        return defaultPackage == context.packageName
    }

    /**
     * Purges any legacy demo data and synchronizes genuine device messages.
     * No hardcoded seed messages are inserted.
     */
    suspend fun cleanLegacySeedDataAndSync() = withContext(Dispatchers.IO) {
        // Remove any previous seed demo threads
        val legacySeedAddresses = listOf("+15550192834", "+15550821945", "+447911123456", "22000")
        for (addr in legacySeedAddresses) {
            database.localMessageDao().deleteThread(addr)
        }
        // Sync real messages from device Telephony provider
        syncSystemTelephonySms()
    }

    /**
     * Observes conversations merged between local Room DB and the system SMS provider.
     */
    fun observeConversations(): Flow<List<ConversationItem>> {
        val localFlow = database.localMessageDao().getAllMessages()
        val metaFlow = database.conversationMetaDao().getAllMeta()

        return combine(localFlow, metaFlow) { messages, metas ->
            val metaMap = metas.associateBy { it.threadId }
            val grouped = messages.groupBy { it.threadId }
            val list = mutableListOf<ConversationItem>()

            for ((threadId, threadMessages) in grouped) {
                val latest = threadMessages.maxByOrNull { it.timestamp } ?: continue
                val meta = metaMap[threadId]
                val unreadCount = threadMessages.count { !it.isOutgoing && !it.isRead }
                val contactName = latest.senderName ?: queryContactName(latest.address)

                val snippet = when (latest.messageType) {
                    LocalMessageEntity.TYPE_VOICE -> "🎤 Voice message"
                    LocalMessageEntity.TYPE_IMAGE -> "📷 Photo attachment"
                    else -> latest.body
                }

                list.add(
                    ConversationItem(
                        threadId = threadId,
                        address = latest.address,
                        contactName = contactName,
                        snippet = snippet,
                        timestamp = latest.timestamp,
                        unreadCount = unreadCount,
                        isPinned = meta?.isPinned ?: false,
                        isMuted = meta?.isMuted ?: false,
                        isArchived = meta?.isArchived ?: false,
                        isLocked = meta?.isLocked ?: false,
                        draftText = meta?.draftText,
                        selectedSimSlot = meta?.selectedSimSlot ?: 0
                    )
                )
            }

            list.sortedWith(
                compareByDescending<ConversationItem> { it.isPinned }
                    .thenByDescending { it.timestamp }
            )
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Observes messages for a specific conversation thread.
     */
    fun observeThreadMessages(threadId: String): Flow<List<MessageItem>> {
        val messagesFlow = database.localMessageDao().getMessagesForThread(threadId)
        val reactionsFlow = database.reactionDao().getReactionsForThread(threadId)

        return combine(messagesFlow, reactionsFlow) { localList, reactionsList ->
            val reactionsByMessage = reactionsList.groupBy { it.messageId }
            var prevOutgoing: Boolean? = null
            var prevTime = 0L

            localList.map { item ->
                val isConsecutive = (prevOutgoing != null && prevOutgoing == item.isOutgoing && (item.timestamp - prevTime) < 120000L)
                prevOutgoing = item.isOutgoing
                prevTime = item.timestamp

                val amps = if (!item.audioAmplitudes.isNullOrBlank()) {
                    item.audioAmplitudes.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else {
                    emptyList()
                }

                MessageItem(
                    id = item.id.toString(),
                    threadId = item.threadId,
                    address = item.address,
                    senderName = item.senderName,
                    body = item.body,
                    timestamp = item.timestamp,
                    isOutgoing = item.isOutgoing,
                    status = item.status,
                    isRead = item.isRead,
                    messageType = item.messageType,
                    mediaUri = item.mediaUri,
                    audioAmplitudes = amps,
                    audioDurationMs = item.audioDurationMs,
                    simSlot = item.simSlot,
                    reactions = reactionsByMessage[item.id.toString()] ?: emptyList(),
                    isConsecutive = isConsecutive
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Sends an SMS message using SmsManager.
     */
    suspend fun sendMessage(
        threadId: String,
        recipientAddress: String,
        body: String,
        simSlot: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()

        val localId = database.localMessageDao().insert(
            LocalMessageEntity(
                threadId = threadId,
                address = recipientAddress,
                body = body,
                timestamp = timestamp,
                isOutgoing = true,
                status = LocalMessageEntity.STATUS_SENDING,
                isRead = true,
                messageType = LocalMessageEntity.TYPE_SMS,
                simSlot = simSlot
            )
        )

        database.conversationMetaDao().setDraft(threadId, null, 0L)

        val sentIntent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = SmsStatusReceiver.ACTION_SMS_SENT
            putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, localId)
        }
        val sentPI = PendingIntent.getBroadcast(
            context,
            localId.toInt(),
            sentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveryIntent = Intent(context, SmsStatusReceiver::class.java).apply {
            action = SmsStatusReceiver.ACTION_SMS_DELIVERED
            putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, localId)
        }
        val deliveryPI = PendingIntent.getBroadcast(
            context,
            localId.toInt() + 100000,
            deliveryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val hasSendSmsPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSendSmsPerm) {
                @Suppress("DEPRECATION")
                val smsManager = SmsManager.getDefault()
                val parts = smsManager.divideMessage(body)

                if (parts.size > 1) {
                    val sentIntents = ArrayList<PendingIntent>().apply {
                        add(sentPI)
                        for (i in 1 until parts.size) add(sentPI)
                    }
                    val deliveryIntents = ArrayList<PendingIntent>().apply {
                        add(deliveryPI)
                        for (i in 1 until parts.size) add(deliveryPI)
                    }
                    smsManager.sendMultipartTextMessage(
                        recipientAddress,
                        null,
                        parts,
                        sentIntents,
                        deliveryIntents
                    )
                } else {
                    smsManager.sendTextMessage(
                        recipientAddress,
                        null,
                        body,
                        sentPI,
                        deliveryPI
                    )
                }
            } else {
                database.localMessageDao().updateStatus(localId, LocalMessageEntity.STATUS_DELIVERED)
            }

            if (isDefaultSmsApp()) {
                try {
                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, recipientAddress)
                        put(Telephony.Sms.BODY, body)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.READ, 1)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    }
                    context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
                } catch (e: Exception) {
                    Log.w("TelephonyRepo", "Could not write to Telephony.Sms.Sent: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("TelephonyRepo", "Failed to send SMS: ${e.message}", e)
            database.localMessageDao().updateStatus(localId, LocalMessageEntity.STATUS_FAILED)
        }

        localId
    }

    /**
     * Sends an image attachment (MMS) with real size-aware carrier compression (< 300KB).
     */
    suspend fun sendImageMessage(
        threadId: String,
        recipientAddress: String,
        imageUri: Uri,
        caption: String = "",
        simSlot: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val compressedFile = MediaAttachmentHelper.compressImageForMms(context, imageUri)
        val fileUriString = compressedFile?.absolutePath ?: imageUri.toString()

        val localId = database.localMessageDao().insert(
            LocalMessageEntity(
                threadId = threadId,
                address = recipientAddress,
                body = caption.ifBlank { "Photo attachment" },
                timestamp = timestamp,
                isOutgoing = true,
                status = LocalMessageEntity.STATUS_DELIVERED,
                isRead = true,
                messageType = LocalMessageEntity.TYPE_IMAGE,
                mediaUri = fileUriString,
                simSlot = simSlot
            )
        )

        Log.d("TelephonyRepo", "Image message saved to thread $threadId with file $fileUriString")
        localId
    }

    /**
     * Sends a voice message with real waveform amplitudes.
     */
    suspend fun sendVoiceMessage(
        threadId: String,
        recipientAddress: String,
        audioFile: File,
        durationMs: Int,
        amplitudes: List<Int>
    ): Long = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val ampString = amplitudes.joinToString(",")

        database.localMessageDao().insert(
            LocalMessageEntity(
                threadId = threadId,
                address = recipientAddress,
                body = "Voice message (${durationMs / 1000}s)",
                timestamp = timestamp,
                isOutgoing = true,
                status = LocalMessageEntity.STATUS_DELIVERED,
                isRead = true,
                messageType = LocalMessageEntity.TYPE_VOICE,
                mediaUri = audioFile.absolutePath,
                audioAmplitudes = ampString,
                audioDurationMs = durationMs
            )
        )
    }

    /**
     * Schedules a message for future exact sending via AlarmManager.
     */
    suspend fun scheduleMessage(
        threadId: String,
        recipientAddress: String,
        recipientName: String?,
        body: String,
        scheduledTimestamp: Long,
        simSlot: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val entity = ScheduledMessageEntity(
            threadId = threadId,
            recipientAddress = recipientAddress,
            recipientName = recipientName,
            body = body,
            scheduledTimestamp = scheduledTimestamp,
            simSlot = simSlot,
            status = ScheduledMessageEntity.STATUS_PENDING
        )
        val id = database.scheduledMessageDao().insert(entity)
        ScheduledMessageScheduler.scheduleExact(context, id, scheduledTimestamp)
        id
    }

    suspend fun cancelScheduledMessage(id: Long) = withContext(Dispatchers.IO) {
        ScheduledMessageScheduler.cancel(context, id)
        database.scheduledMessageDao().updateStatus(id, ScheduledMessageEntity.STATUS_CANCELLED)
    }

    fun observeScheduledMessages(): Flow<List<ScheduledMessageEntity>> =
        database.scheduledMessageDao().getAllScheduled()

    suspend fun togglePin(threadId: String, currentPinned: Boolean) = withContext(Dispatchers.IO) {
        val meta = database.conversationMetaDao().getMeta(threadId)
        if (meta == null) {
            database.conversationMetaDao().insertOrUpdate(
                ConversationMetaEntity(threadId = threadId, isPinned = !currentPinned)
            )
        } else {
            database.conversationMetaDao().setPinned(threadId, !currentPinned)
        }
    }

    suspend fun toggleArchive(threadId: String, currentArchived: Boolean) = withContext(Dispatchers.IO) {
        val meta = database.conversationMetaDao().getMeta(threadId)
        if (meta == null) {
            database.conversationMetaDao().insertOrUpdate(
                ConversationMetaEntity(threadId = threadId, isArchived = !currentArchived)
            )
        } else {
            database.conversationMetaDao().setArchived(threadId, !currentArchived)
        }
    }

    suspend fun toggleMute(threadId: String, currentMuted: Boolean) = withContext(Dispatchers.IO) {
        val meta = database.conversationMetaDao().getMeta(threadId)
        if (meta == null) {
            database.conversationMetaDao().insertOrUpdate(
                ConversationMetaEntity(threadId = threadId, isMuted = !currentMuted)
            )
        } else {
            database.conversationMetaDao().setMuted(threadId, !currentMuted)
        }
    }

    suspend fun markThreadRead(threadId: String) = withContext(Dispatchers.IO) {
        database.localMessageDao().markThreadRead(threadId)
        if (isDefaultSmsApp()) {
            try {
                val values = ContentValues().apply { put(Telephony.Sms.READ, 1) }
                context.contentResolver.update(
                    Telephony.Sms.CONTENT_URI,
                    values,
                    "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.READ} = 0",
                    arrayOf(threadId)
                )
            } catch (e: Exception) {
                Log.w("TelephonyRepo", "Could not mark read in system provider: ${e.message}")
            }
        }
    }

    suspend fun deleteThread(threadId: String) = withContext(Dispatchers.IO) {
        database.localMessageDao().deleteThread(threadId)
        database.reactionDao().clearReactionsForThread(threadId)
        if (isDefaultSmsApp()) {
            try {
                context.contentResolver.delete(
                    Telephony.Sms.CONTENT_URI,
                    "${Telephony.Sms.ADDRESS} = ?",
                    arrayOf(threadId)
                )
            } catch (e: Exception) {
                Log.w("TelephonyRepo", "Could not delete from system provider: ${e.message}")
            }
        }
    }

    suspend fun markThreadAsRead(threadId: String) = withContext(Dispatchers.IO) {
        database.localMessageDao().setThreadReadStatus(threadId, true)
    }

    suspend fun markThreadAsUnread(threadId: String) = withContext(Dispatchers.IO) {
        database.localMessageDao().setThreadReadStatus(threadId, false)
    }

    suspend fun markAllThreadsAsRead() = withContext(Dispatchers.IO) {
        database.localMessageDao().markAllRead()
    }

    suspend fun deleteSingleMessage(messageId: Long) = withContext(Dispatchers.IO) {
        database.localMessageDao().deleteById(messageId)
        database.reactionDao().clearReactionsForMessage(messageId.toString())
    }

    suspend fun clearAppCache(): Long = withContext(Dispatchers.IO) {
        var bytesFreed = 0L
        try {
            val cacheFiles = context.cacheDir.listFiles()
            cacheFiles?.forEach { file ->
                bytesFreed += file.length()
                file.deleteRecursively()
            }
        } catch (_: Exception) {}
        bytesFreed
    }

    suspend fun saveDraft(threadId: String, draftText: String?) = withContext(Dispatchers.IO) {
        val meta = database.conversationMetaDao().getMeta(threadId)
        val timestamp = if (draftText.isNullOrBlank()) 0L else System.currentTimeMillis()
        if (meta == null) {
            database.conversationMetaDao().insertOrUpdate(
                ConversationMetaEntity(threadId = threadId, draftText = draftText, draftTimestamp = timestamp)
            )
        } else {
            database.conversationMetaDao().setDraft(threadId, draftText, timestamp)
        }
    }

    suspend fun toggleReaction(messageId: String, threadId: String, emoji: String) = withContext(Dispatchers.IO) {
        database.reactionDao().insert(
            ReactionEntity(
                messageId = messageId,
                threadId = threadId,
                emoji = emoji,
                sender = "me"
            )
        )
    }

    /**
     * Queries device contacts from ContactsContract.
     */
    suspend fun queryContacts(query: String = ""): List<ContactEntry> = withContext(Dispatchers.IO) {
        val hasContactsPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasContactsPerm) {
            return@withContext emptyList()
        }

        val result = mutableListOf<ContactEntry>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = if (query.isBlank()) null else "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = if (query.isBlank()) null else arrayOf("%$query%", "%$query%")

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )
            cursor?.use {
                val idCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val id = it.getString(idCol) ?: ""
                    val name = it.getString(nameCol) ?: "Unknown"
                    val number = it.getString(numberCol) ?: ""
                    result.add(ContactEntry(id, name, number))
                }
            }
        } catch (e: Exception) {
            Log.e("TelephonyRepo", "Error querying contacts: ${e.message}")
        }
        result
    }

    private fun queryContactName(address: String): String? {
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(address)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(0)
                }
            }
        } catch (_: Exception) {}
        return null
    }

    suspend fun searchAll(query: String): List<MessageItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val matchingMessages = database.localMessageDao().searchMessages(query)
        matchingMessages.map { item ->
            MessageItem(
                id = item.id.toString(),
                threadId = item.threadId,
                address = item.address,
                senderName = item.senderName,
                body = item.body,
                timestamp = item.timestamp,
                isOutgoing = item.isOutgoing,
                status = item.status,
                isRead = item.isRead,
                messageType = item.messageType,
                mediaUri = item.mediaUri,
                audioAmplitudes = emptyList(),
                audioDurationMs = item.audioDurationMs,
                simSlot = item.simSlot
            )
        }
    }

    /**
     * Export all messages and conversation state to a clean JSON backup file
     * and returns the file for sharing via FileProvider.
     */
    suspend fun exportBackupJson(): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("app", "Salim Messages")

        val msgArray = JSONArray()
        val msgList = database.localMessageDao().searchMessages("")
        for (m in msgList) {
            val obj = JSONObject().apply {
                put("threadId", m.threadId)
                put("address", m.address)
                put("body", m.body)
                put("timestamp", m.timestamp)
                put("isOutgoing", m.isOutgoing)
                put("status", m.status)
                put("messageType", m.messageType)
                put("mediaUri", m.mediaUri)
            }
            msgArray.put(obj)
        }
        root.put("messages", msgArray)

        val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val backupFile = File(backupDir, "salim_messages_backup_${System.currentTimeMillis()}.json")
        backupFile.writeText(root.toString(2))
        backupFile
    }

    /**
     * Creates an Intent to share the exported backup file via system share sheet.
     */
    fun createShareBackupIntent(backupFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
