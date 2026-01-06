package org.bibletranslationtools.glossary.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Verse

@Composable
fun ChapterVerse(
    verse: Verse,
    ref: Ref,
    phrase: String,
    focusOnVerse: Boolean
) {
    val color = if (!focusOnVerse || verse.number == ref.verse) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.outline
    }

    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                baselineShift = BaselineShift.Superscript
            )
        ) {
            append(verse.number)
            append(" ")
        }

        if (ref.verse == verse.number) {
            var lastIndex = 0
            val regex = Regex(
                "\\b${Regex.escape(phrase)}\\b",
                RegexOption.IGNORE_CASE
            )
            regex.findAll(verse.text).forEach { match ->
                withStyle(
                    style = SpanStyle(color = color)
                ) {
                    append(verse.text.substring(lastIndex, match.range.first))
                }
                withStyle(
                    style = SpanStyle(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(match.value)
                }
                lastIndex = match.range.last + 1
            }
            if (lastIndex < verse.text.length) {
                withStyle(
                    style = SpanStyle(color = color)
                ) {
                    append(verse.text.substring(lastIndex))
                }
            }
        } else {
            append(verse.text)
        }
    }

    Text(
        text = annotatedString,
        color = color
    )
}