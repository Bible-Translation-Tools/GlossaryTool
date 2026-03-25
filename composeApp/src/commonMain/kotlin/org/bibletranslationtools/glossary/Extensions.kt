package org.bibletranslationtools.glossary

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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

fun Modifier.positionAwareImePadding() = composed {
    var consumePadding by remember { mutableIntStateOf(0) }
    this@positionAwareImePadding
        .onGloballyPositioned { coordinates ->
            val rootCoordinate = coordinates.findRootCoordinates()
            val bottom = coordinates.positionInWindow().y + coordinates.size.height
            consumePadding = (rootCoordinate.size.height - bottom).toInt()
        }
        .consumeWindowInsets(PaddingValues(bottom = consumePadding.pxToDp()))
        .imePadding()
}

@Composable
fun Int.pxToDp(): Dp {
    val density = LocalDensity.current
    return with(density) { this@pxToDp.toDp() }
}