package com.example

import com.example.data.telephony.PhoneNumberHelper
import com.example.data.telephony.SmsSegmentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testGsm7Segmentation_shortAscii() {
        val result = SmsSegmentation.calculate("Hello Salim Messages")
        assertFalse("Should be GSM-7 encoded", result.isUcs2)
        assertEquals(1, result.segmentCount)
        assertEquals(160 - 20, result.remainingInSegment)
    }

    @Test
    fun testGsm7Segmentation_multipart() {
        val longText = "A".repeat(170)
        val result = SmsSegmentation.calculate(longText)
        assertFalse("Should be GSM-7 encoded", result.isUcs2)
        assertEquals(2, result.segmentCount)
        // Multi-part GSM-7 limit is 153 per segment; 170 chars = 153 + 17 -> remaining = 153 - 17 = 136
        assertEquals(136, result.remainingInSegment)
    }

    @Test
    fun testUcs2Segmentation_emoji() {
        val result = SmsSegmentation.calculate("Hello 👋")
        assertTrue("Should detect UCS-2 encoding for emoji", result.isUcs2)
        assertEquals(1, result.segmentCount)
        assertEquals(70 - "Hello 👋".length, result.remainingInSegment)
    }

    @Test
    fun testUcs2Segmentation_arabicAndCyrillic() {
        val resultArabic = SmsSegmentation.calculate("مرحبا بكم")
        assertTrue("Arabic must use UCS-2", resultArabic.isUcs2)
        assertEquals(1, resultArabic.segmentCount)

        val longCyrillic = "Привет ".repeat(15) // ~105 chars > 70
        val resultCyrillic = SmsSegmentation.calculate(longCyrillic)
        assertTrue("Cyrillic must use UCS-2", resultCyrillic.isUcs2)
        assertEquals(2, resultCyrillic.segmentCount)
    }

    @Test
    fun testPhoneNumberHelper_normalize() {
        assertEquals("+15551234567", PhoneNumberHelper.normalize("+1 (555) 123-4567"))
        assertEquals("5551234567", PhoneNumberHelper.normalize("(555) 123-4567"))
        assertEquals("+447911123456", PhoneNumberHelper.normalize("+44 7911 123456"))
    }

    @Test
    fun testPhoneNumberHelper_compare() {
        assertTrue(PhoneNumberHelper.compare("+1 (555) 123-4567", "5551234567"))
        assertTrue(PhoneNumberHelper.compare("1234567", "+15551234567"))
        assertFalse(PhoneNumberHelper.compare("5551111", "5552222"))
    }
}
