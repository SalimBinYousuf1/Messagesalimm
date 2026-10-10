package com.example.data.telephony

/**
 * Real GSM-7 vs UCS-2 character segmentation calculations per 3GPP TS 23.038 specification.
 */
object SmsSegmentation {

    // GSM 7-bit basic character set
    private val GSM_7_BASIC = setOf(
        '@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
        'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', '\u001b', 'Æ', 'æ', 'ß', 'É',
        ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
        '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'
    )

    // GSM 7-bit extended character set (each takes 2 GSM septets via escape character)
    private val GSM_7_EXTENDED = setOf(
        '\u000c', // form feed
        '^',
        '{',
        '}',
        '\\',
        '[',
        '~',
        ']',
        '|',
        '€'
    )

    data class SegmentResult(
        val isUcs2: Boolean,
        val totalChars: Int,
        val segmentCount: Int,
        val remainingInSegment: Int,
        val maxCharsInSingleSegment: Int,
        val maxCharsPerMultipartSegment: Int
    )

    fun calculate(text: String): SegmentResult {
        if (text.isEmpty()) {
            return SegmentResult(
                isUcs2 = false,
                totalChars = 0,
                segmentCount = 1,
                remainingInSegment = 160,
                maxCharsInSingleSegment = 160,
                maxCharsPerMultipartSegment = 153
            )
        }

        var isUcs2 = false
        var gsmUnits = 0

        for (ch in text) {
            when {
                ch in GSM_7_BASIC -> gsmUnits += 1
                ch in GSM_7_EXTENDED -> gsmUnits += 2 // Escaped char takes 2 units
                else -> {
                    isUcs2 = true
                    break
                }
            }
        }

        return if (isUcs2) {
            // UCS-2 (Unicode) segmentation:
            // Single part: 70 chars. Multi-part: 67 chars per part.
            val charCount = text.length
            val segmentLimit = if (charCount <= 70) 70 else 67
            val segmentCount = if (charCount <= 70) 1 else ((charCount - 1) / 67) + 1
            val remaining = if (charCount <= 70) {
                70 - charCount
            } else {
                val inCurrent = charCount % 67
                if (inCurrent == 0) 0 else 67 - inCurrent
            }

            SegmentResult(
                isUcs2 = true,
                totalChars = charCount,
                segmentCount = segmentCount,
                remainingInSegment = remaining,
                maxCharsInSingleSegment = 70,
                maxCharsPerMultipartSegment = 67
            )
        } else {
            // GSM-7 segmentation:
            // Single part: 160 septets. Multi-part: 153 septets per part.
            val segmentLimit = if (gsmUnits <= 160) 160 else 153
            val segmentCount = if (gsmUnits <= 160) 1 else ((gsmUnits - 1) / 153) + 1
            val remaining = if (gsmUnits <= 160) {
                160 - gsmUnits
            } else {
                val inCurrent = gsmUnits % 153
                if (inCurrent == 0) 0 else 153 - inCurrent
            }

            SegmentResult(
                isUcs2 = false,
                totalChars = gsmUnits,
                segmentCount = segmentCount,
                remainingInSegment = remaining,
                maxCharsInSingleSegment = 160,
                maxCharsPerMultipartSegment = 153
            )
        }
    }
}
