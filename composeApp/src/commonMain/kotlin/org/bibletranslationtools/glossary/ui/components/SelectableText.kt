@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.Selection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.bibletranslationtools.glossary.data.Phrase

private const val HIGHLIGHT_TAG = "highlight"

// Opt-in for onSelectionChange, which is an internal API.
@OptIn(InternalTextApi::class)
@Composable
fun SelectableText(
    text: String,
    phrases: List<Phrase>,
    selectedText: String,
    onSelectedTextChanged: (String) -> Unit,
    onPhraseClick: (Phrase) -> Unit,
    modifier: Modifier = Modifier
) {
    var selection by remember { mutableStateOf<Selection?>(null) }
    val highlightColor = MaterialTheme.colorScheme.onBackground

    val annotatedString = remember(text, phrases) {
        buildAnnotatedString {
            append(text)
            phrases.forEach { phrase ->
                val regex = Regex(
                    pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
                    option = RegexOption.IGNORE_CASE
                )
                regex.findAll(text).forEach { matchResult ->
                    val startIndex = matchResult.range.first
                    val endIndex = matchResult.range.last + 1

                    addStyle(
                        style = SpanStyle(
                            color = highlightColor,
                            fontWeight = FontWeight.Bold
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                    addStringAnnotation(
                        tag = HIGHLIGHT_TAG,
                        annotation = phrase.phrase,
                        start = startIndex,
                        end = endIndex
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedText) {
        if (selectedText.isEmpty()) {
            selection = null
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    SelectionContainer(
        selection = selection,
        onSelectionChange = { newSelection ->
            selection = newSelection
            val newSelectedText = newSelection?.let {
                text.substring(it.start.offset, it.end.offset)
            } ?: ""
            onSelectedTextChanged(newSelectedText)
        },
        modifier = modifier
    ) {
        Text(
            text = annotatedString,
            style = LocalTextStyle.current.copy(lineHeight = 24.sp),
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        textLayoutResult?.let { layoutResult ->
                            val position = layoutResult.getOffsetForPosition(offset)
                            annotatedString
                                .getStringAnnotations(tag = HIGHLIGHT_TAG, start = position, end = position)
                                .firstOrNull()
                                ?.let { annotation ->
                                    onSelectedTextChanged("")
                                    onPhraseClick(phrases.single { it.phrase == annotation.item })
                                }
                        }
                    }
                }
        )
    }
}