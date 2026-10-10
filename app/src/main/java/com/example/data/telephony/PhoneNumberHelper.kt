package com.example.data.telephony

import android.telephony.PhoneNumberUtils
import java.util.Locale

object PhoneNumberHelper {

    /**
     * Normalizes a phone number to standard digits or E.164-like representation.
     * Removes spaces, hyphens, parentheses, etc.
     */
    fun normalize(rawNumber: String): String {
        if (rawNumber.isBlank()) return ""
        val trimmed = rawNumber.trim()
        val hasPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.filter { it.isDigit() }
        return if (hasPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Checks if two phone numbers match (using standard 7 or 10 digit comparison).
     */
    fun compare(numberA: String, numberB: String): Boolean {
        val normA = normalize(numberA)
        val normB = normalize(numberB)
        if (normA == normB) return true
        if (normA.isEmpty() || normB.isEmpty()) return false

        // Compare last 7-10 digits for regional carrier prefixes
        val minLen = minOf(normA.length, normB.length)
        if (minLen >= 7) {
            val suffixA = normA.takeLast(7)
            val suffixB = normB.takeLast(7)
            return suffixA == suffixB
        }
        return false
    }

    /**
     * Formats number for display nicely (e.g., (555) 123-4567 or international).
     */
    fun formatForDisplay(rawNumber: String): String {
        if (rawNumber.isBlank()) return ""
        val countryIso = Locale.getDefault().country
        return PhoneNumberUtils.formatNumber(rawNumber, countryIso) ?: rawNumber
    }
}
