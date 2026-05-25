package org.bibletranslationtools.glossary

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun testStringNormalize() {
        val decomposed = "n\u0303" // n + tilde
        val composed = "\u00f1" // ñ
        assertEquals(composed, decomposed.normalize())
    }

    @Test
    fun testLongToLocalDateTime() {
        val timestamp: Long = 1716382800 // 2024-05-22 13:00:00 UTC
        val localDateTime = timestamp.toLocalDateTime()
        
        // Note: TimeZone.currentSystemDefault() is used, so exact hour might vary,
        // but year, month, day should be consistent for this specific timestamp in most zones.
        assertEquals(2024, localDateTime.year)
        assertEquals(5, localDateTime.month.number)
        assertEquals(22, localDateTime.day)
    }

    @Test
    fun testStringToLocalDateTime() {
        val isoString = "2024-05-22T13:00:00"
        val localDateTime = isoString.toLocalDateTime()
        assertEquals(2024, localDateTime.year)
        assertEquals(5, localDateTime.month.number)
        assertEquals(22, localDateTime.day)
        assertEquals(13, localDateTime.hour)
    }

    @Test
    fun testLocalDateTimeToTimestamp() {
        val ldt = LocalDateTime(2024, 5, 22, 13, 0, 0)
        val timestamp = ldt.toTimestamp()
        val backToLdt = timestamp.toLocalDateTime()
        
        assertEquals(ldt.year, backToLdt.year)
        assertEquals(ldt.month.number, backToLdt.month.number)
        assertEquals(ldt.day, backToLdt.day)
        assertEquals(ldt.hour, backToLdt.hour)
    }
}
