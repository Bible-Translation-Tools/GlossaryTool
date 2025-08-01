package org.bibletranslationtools.glossary

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime(): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = Instant.fromEpochSeconds(this)
    return instant.toLocalDateTime(timeZone)
}

@OptIn(ExperimentalTime::class)
fun LocalDateTime.toTimestamp(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    return this.toInstant(timeZone).epochSeconds
}

@OptIn(ExperimentalTime::class)
fun String.toLocalDateTime(): LocalDateTime {
    runCatching {
        val instant = Instant.parse(this)
        return instant.toLocalDateTime(TimeZone.UTC)
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