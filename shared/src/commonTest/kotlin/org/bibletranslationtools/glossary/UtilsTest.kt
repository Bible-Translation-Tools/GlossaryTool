package org.bibletranslationtools.glossary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun testRandomStringLength() {
        val length = 10
        val result = Utils.randomString(length)
        assertEquals(length, result.length)
    }

    @Test
    fun testRandomStringUniqueness() {
        val s1 = Utils.randomString(10)
        val s2 = Utils.randomString(10)
        assertNotEquals(s1, s2)
    }

    @Test
    fun testRandomCodeLength() {
        val result = Utils.randomCode()
        assertEquals(5, result.length)
    }

    @Test
    fun testRandomCodeCharacters() {
        val result = Utils.randomCode()
        val excludedChars = listOf('O', '0', 'I', '1', 'L', 'S', '5', 'Z', '2', 'B', '8')
        result.forEach { char ->
            assertTrue(char !in excludedChars, "Code contains excluded character: $char")
        }
    }

    @Test
    fun testStringNormalization() {
        // "e" + combining acute accent (decomposed)
        val decomposed = "e\u0301"
        // "é" (composed)
        val composed = "\u00e9"
        
        assertNotEquals(decomposed, composed)
        assertEquals(composed, decomposed.normalize())
    }
}
