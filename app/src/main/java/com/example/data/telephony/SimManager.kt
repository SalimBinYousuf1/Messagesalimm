package com.example.data.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SimCardInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String?
)

class SimManager(private val context: Context) {

    private val subscriptionManager: SubscriptionManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        } else {
            null
        }
    }

    fun getActiveSimCards(): List<SimCardInfo> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission || subscriptionManager == null) {
            return listOf(
                SimCardInfo(
                    subscriptionId = -1,
                    slotIndex = 0,
                    displayName = "SIM 1 (Default)",
                    carrierName = "Carrier",
                    number = null
                )
            )
        }

        return try {
            val list = subscriptionManager?.activeSubscriptionInfoList
            if (list.isNullOrEmpty()) {
                listOf(
                    SimCardInfo(
                        subscriptionId = -1,
                        slotIndex = 0,
                        displayName = "SIM 1 (Default)",
                        carrierName = "Carrier",
                        number = null
                    )
                )
            } else {
                list.map { info: SubscriptionInfo ->
                    SimCardInfo(
                        subscriptionId = info.subscriptionId,
                        slotIndex = info.simSlotIndex,
                        displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                        carrierName = info.carrierName?.toString() ?: "Carrier",
                        number = info.number
                    )
                }
            }
        } catch (e: SecurityException) {
            listOf(
                SimCardInfo(
                    subscriptionId = -1,
                    slotIndex = 0,
                    displayName = "SIM 1 (Default)",
                    carrierName = "Carrier",
                    number = null
                )
            )
        }
    }
}
