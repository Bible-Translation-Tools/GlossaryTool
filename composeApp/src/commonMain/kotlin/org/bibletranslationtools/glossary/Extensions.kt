package org.bibletranslationtools.glossary

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Long.toLocalDateTime(): LocalDateTime {
    val timeZone = TimeZone.currentSystemDefault()
    val instant = Instant.fromEpochSeconds(this)
    return instant.toLocalDateTime(timeZone)
}

fun LocalDateTime.toTimestamp(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    return this.toInstant(timeZone).epochSeconds
}