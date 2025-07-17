package org.bibletranslationtools.glossary

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object Utils {
    fun randomString(length: Int): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun randomCode(): String {
        val charPool = (('A'..'Z') + ('0'..'9'))
            .filterNot { char ->
                when (char) {
                    'O', '0', // Zero and O
                    'I', '1', // One and I
                    'L',      // L (can look like 1 or I)
                    'S', '5', // Five and S
                    'Z', '2', // Two and Z
                    'B', '8'  // Eight and B
                        -> true
                    else -> false
                }
            }
        return (1..5)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateUUID(): String {
        return Uuid.random().toString()
    }

    fun getCurrentTime() =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun getCurrentTimestamp() = Clock.System.now().epochSeconds
}