package org.bibletranslationtools.glossary

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import java.text.Normalizer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Normalize all decomposed unicode characters to composed
 */
fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFC)
}

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime(): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = Instant.fromEpochSeconds(this)
    return instant.toLocalDateTime(timeZone)
}

@OptIn(ExperimentalTime::class)
fun String.toLocalDateTime(): LocalDateTime {
    runCatching {
        val instant = Instant.parse(this)
        val timeZone = TimeZone.currentSystemDefault()
        return instant.toLocalDateTime(timeZone)
    }

    runCatching {
        return LocalDateTime.parse(this)
    }

    runCatching {
        val date = LocalDate.parse(this)
        return date.atTime(0, 0)
    }

    return getCurrentTime()
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toTimestamp(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    return this.toInstant(timeZone).epochSeconds
}

fun <T : Any> Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { value ->
        trySend(value)
    }
    awaitClose { cancellation.cancel() }
}
