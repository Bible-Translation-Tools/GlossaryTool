package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerseReference(reference: String, phrase: String, text: String) {
    val style = TextStyle.Default.copy(
        lineHeight = 28.sp,
        fontSize = 16.sp
    )

    val referenceTag = "reference"
    val referenceView = InlineTextContent(
        placeholder = Placeholder(
            width = (reference.length * 11).sp,
            height = style.lineHeight,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = reference,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    val annotatedText = buildAnnotatedString {
        appendInlineContent(referenceTag, reference)

        var lastIndex = 0
        val regex = Regex(
            pattern = "\\b${Regex.escape(phrase)}\\b",
            option = RegexOption.IGNORE_CASE
        )
        regex.findAll(text).forEach { match ->
            val startIndex = match.range.first
            val endIndex = match.range.last + 1

            append(text.substring(lastIndex, startIndex))

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(match.value)
            }

            lastIndex = endIndex
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    Text(
        text = annotatedText,
        style = style,
        inlineContent = mapOf("reference" to referenceView),
    )
}