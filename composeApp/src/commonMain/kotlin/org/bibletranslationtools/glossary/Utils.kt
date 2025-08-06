package org.bibletranslationtools.glossary

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object Utils {
    val JsonLenient = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

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

    @OptIn(ExperimentalTime::class)
    fun getCurrentTime() =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    @OptIn(ExperimentalTime::class)
    fun getCurrentTimestamp() = Clock.System.now().epochSeconds

    fun slideHorizontally(): StackAnimator {
        return slide(
            animationSpec = customTween(),
            orientation = Orientation.Horizontal
        )
    }

    fun slideVertically(): StackAnimator {
        return slide(
            animationSpec = customTween(),
            orientation = Orientation.Vertical
        )
    }

    private fun <T> customTween(): TweenSpec<T> {
        return tween(
            delayMillis = 20,
            durationMillis = 300,
            easing = EaseIn
        )
    }
}