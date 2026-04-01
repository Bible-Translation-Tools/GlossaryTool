package org.bibletranslationtools.glossary

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.bibletranslationtools.glossary.toLocalDateTime as toLocalDateTimeExt

object CustomLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CustomLocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return string.toLocalDateTimeExt()
    }
}

object Utils {
    val JsonLenient = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true

        serializersModule = SerializersModule {
            contextual(LocalDateTime::class, CustomLocalDateTimeSerializer)
        }
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

    fun bookOrderMap(): Map<String, Int> {
        return mapOf(
            "gen" to 1, "exo" to 2, "lev" to 3, "num" to 4, "deu" to 5, "jos" to 6,
            "jdg" to 7, "rut" to 8, "1sa" to 9, "2sa" to 10, "1ki" to 11, "2ki" to 12,
            "1ch" to 13, "2ch" to 14, "ezr" to 15, "neh" to 16, "est" to 17, "job" to 18,
            "psa" to 19, "pro" to 20, "ecc" to 21, "sng" to 22, "isa" to 23, "jer" to 24,
            "lam" to 25, "ezk" to 26, "dan" to 27, "hos" to 28, "jol" to 29, "amo" to 30,
            "oba" to 31, "jon" to 32, "mic" to 33, "nah" to 34, "hab" to 35, "zep" to 36,
            "hag" to 37, "zec" to 38, "mal" to 39, "mat" to 41, "mrk" to 42, "luk" to 43,
            "jhn" to 44, "act" to 45, "rom" to 46, "1co" to 47, "2co" to 48, "gal" to 49,
            "eph" to 50, "php" to 51, "col" to 52, "1th" to 53, "2th" to 54, "1ti" to 55,
            "2ti" to 56, "tit" to 57, "phm" to 58, "heb" to 59, "jas" to 60, "1pe" to 61,
            "2pe" to 62, "1jn" to 63, "2jn" to 64, "3jn" to 65, "jud" to 66, "rev" to 67
        )
    }
}