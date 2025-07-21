@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.selection.Selection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_to_glossary
import glossary.composeapp.generated.resources.view_glossary
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

private const val HIGHLIGHT_TAG = "highlight"
private const val VERSE_TAG = "verse"

// Opt-in for onSelectionChange, which is an internal API.
@OptIn(InternalTextApi::class)
@Composable
fun SelectableText(
    chapter: Chapter,
    phrases: List<Phrase>,
    selectedText: String,
    onSelectedTextChanged: (String) -> Unit,
    onSaveSelection: (String) -> Unit,
    onPhraseClick: (Phrase, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightColor = MaterialTheme.colorScheme.onBackground

    val currentChapter by rememberUpdatedState(newValue = chapter)
    val currentPhrases by rememberUpdatedState(newValue = phrases)

    var selection by remember { mutableStateOf<Selection?>(null) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var text by remember(currentChapter, currentPhrases) { mutableStateOf("") }

    var annotatedString by remember { mutableStateOf(buildAnnotatedString{}) }

    LaunchedEffect(selectedText) {
        if (selectedText.isEmpty()) {
            selection = null
        }
    }

    LaunchedEffect(currentChapter, currentPhrases) {
        annotatedString = buildAnnotatedString {
            var lastIndex = 0
            currentChapter.verses.forEach { verse ->
                val verseText = "${verse.number} ${verse.text} "
                append(verseText)
                text += verseText

                addStringAnnotation(
                    tag = VERSE_TAG,
                    annotation = verse.number,
                    start = lastIndex,
                    end = lastIndex + verseText.length
                )

                lastIndex += verseText.length
            }

            currentPhrases.forEach { phrase ->
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

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalTextToolbar provides EmptyTextToolbar) {
            SelectionContainer(
                selection = selection,
                onSelectionChange = { newSelection ->
                    selection = newSelection
                    val newSelectedText = newSelection?.let { sel ->
                        val start = min(sel.start.offset, sel.end.offset)
                        val end = max(sel.start.offset, sel.end.offset)
                        text.substring(start, end)
                    } ?: ""
                    onSelectedTextChanged(newSelectedText)
                },
                modifier = modifier
            ) {
                Text(
                    text = annotatedString,
                    style = LocalTextStyle.current.copy(lineHeight = 32.sp),
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                textLayoutResult?.let { layoutResult ->
                                    val position = layoutResult.getOffsetForPosition(offset)
                                    val phrase = annotatedString
                                        .getStringAnnotations(
                                            tag = HIGHLIGHT_TAG,
                                            start = position,
                                            end = position
                                        )
                                        .firstOrNull()
                                        ?.let { annotation ->
                                            currentPhrases.single { it.phrase == annotation.item }
                                        }
                                    val verse = annotatedString
                                        .getStringAnnotations(
                                            tag = VERSE_TAG,
                                            start = position,
                                            end = position
                                        )
                                        .firstOrNull()?.item

                                    phrase?.let { p ->
                                        verse?.let { v ->
                                            onSelectedTextChanged("")
                                            onPhraseClick(p, v)
                                        }
                                    }
                                }
                            }
                        }
                )
            }
        }

        selection?.let { sel ->
            textLayoutResult?.let { layoutResult ->
                val selectionBoundingBox = layoutResult.getPathForRange(
                    start = min(sel.start.offset, sel.end.offset),
                    end = max(sel.start.offset, sel.end.offset)
                ).getBounds()

                val density = LocalDensity.current
                val offsetX = with(density) { selectionBoundingBox.center.x.toDp() }
                val offsetY = with(density) { selectionBoundingBox.top.toDp() }

                Button(
                    onClick = {
                        onSaveSelection(selectedText)
                        onSelectedTextChanged("")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .graphicsLayer {
                            translationY = -size.height - 8.dp.toPx()
                            translationX = -size.width / 2
                        }
                ) {
                    val isView = currentPhrases.any {
                        it.phrase.lowercase() == selectedText.lowercase()
                    } || selectedText.isEmpty()

                    val text = if (isView) {
                        stringResource(Res.string.view_glossary)
                    } else {
                        stringResource(Res.string.add_to_glossary)
                    }
                    Text(text)
                }
            }
        }
    }
}