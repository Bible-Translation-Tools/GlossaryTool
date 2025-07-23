package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize.Companion.StepBased
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.learn_more
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.ui.screenmodel.PhraseDetails
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhraseDetailsBar(
    details: PhraseDetails,
    onViewDetails: (Phrase) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPhrase by remember { mutableStateOf(details.phrase) }
    var currentRef by remember { mutableStateOf<Ref?>(null) }
    var initialVerse by remember { mutableStateOf<String?>(details.verse) }
    var currentVerseText by remember { mutableStateOf("") }

    LaunchedEffect(currentPhrase) {
        currentRef = findRef(
            details,
            currentPhrase,
            initialVerse
        )
    }

    LaunchedEffect(currentPhrase, currentRef) {
        val ref = currentRef ?: Ref(
            resource = details.resource.slug,
            book = details.book.slug,
            chapter = details.chapter.number.toString(),
            verse = details.verse
        )
        val text = ref.getText(details.resource)
        currentVerseText = shortenVerseText(text, currentPhrase)
    }

    LaunchedEffect(Unit) {
        initialVerse = null
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .clickable(enabled = false, onClick = {})
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        findPhrase(details, currentPhrase, -1)
                            ?.let { currentPhrase = it }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentPhrase.phrase,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentPhrase.spelling,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play pronunciation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        findPhrase(details, currentPhrase, 1)
                            ?.let { currentPhrase = it }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                val annotatedVerse = buildAnnotatedString {
                    append(currentVerseText)

                    val regex = Regex(
                        pattern = "\\b${Regex.escape(currentPhrase.phrase)}\\b",
                        option = RegexOption.IGNORE_CASE
                    )
                    regex.findAll(currentVerseText).forEach { matchResult ->
                        val startIndex = matchResult.range.first
                        val endIndex = matchResult.range.last + 1
                        addStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            ),
                            start = startIndex,
                            end = endIndex
                        )
                    }
                }
                Text(
                    text = annotatedVerse,
                    minLines = 3,
                    maxLines = 3,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        IconButton(onClick = {
                            findRef(currentPhrase, currentRef, -1)
                                ?.let { currentRef = it }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Ref",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        BasicText(
                            text = currentRef.toString(),
                            autoSize = StepBased(
                                minFontSize = 12.sp,
                                maxFontSize = 16.sp
                            ),
                            softWrap = false,
                            style = TextStyle.Default.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            findRef(currentPhrase, currentRef, 1)
                                ?.let { currentRef = it }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Ref",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onViewDetails(currentPhrase)
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.height(48.dp)
                            .width(180.dp)
                            .weight(0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.learn_more),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
    }
}

private fun findPhrase(
    details: PhraseDetails,
    phrase: Phrase,
    incr: Int
): Phrase? {
    return details.phrases.getOrNull(
        details.phrases.indexOf(phrase) + incr
    )
}

private fun findRef(
    details: PhraseDetails,
    phrase: Phrase,
    verse: String?
): Ref? {
    return phrase.refs.firstOrNull {
        it.resource == details.resource.slug
                && it.book == details.book.slug
                && it.chapter == details.chapter.number.toString()
                && (verse == null || it.verse == verse)
    }
}

private fun findRef(
    phrase: Phrase,
    ref: Ref?,
    incr: Int
): Ref? {
    return phrase.refs
        .getOrNull(phrase.refs.indexOf(ref) + incr)
}

private fun shortenVerseText(text: String, phrase: Phrase): String {
    val index = text.lowercase().indexOf(phrase.phrase.lowercase())

    if (index > 50) {
        val start = index - 50
        val end = text.length
        return "...${text.substring(start, end)}"
    }
    return text
}