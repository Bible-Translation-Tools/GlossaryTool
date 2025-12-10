package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.cancel
import glossary.composeapp.generated.resources.save_changes
import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.DeltaType
import org.bibletranslationtools.glossary.data.api.PendingPhrase
import org.bibletranslationtools.glossary.data.api.ReviewStatus
import org.bibletranslationtools.glossary.data.api.User
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReviewPendingPhraseBar(
    pendingPhrase: PendingPhrase,
    me: User,
    onSave: (ReviewStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(ReviewStatus.REJECTED) }
    var compareDiff by remember { mutableStateOf(false) }

    var spellingDiff by remember { mutableStateOf(AnnotatedString("")) }
    var descriptionDiff by remember { mutableStateOf(AnnotatedString("")) }

    LaunchedEffect(me) {
        val myStatus = pendingPhrase.reviews.find {
            it.user.username == me.username
        }?.status ?: ReviewStatus.REJECTED
        selectedStatus = myStatus
    }

    LaunchedEffect(compareDiff) {
        if (compareDiff) {
            spellingDiff = generateAnnotatedDiff(
                oldText = pendingPhrase.original?.spelling ?: "",
                newText = pendingPhrase.phrase.spelling
            )
            descriptionDiff = generateAnnotatedDiff(
                oldText = pendingPhrase.original?.description ?: "",
                newText = pendingPhrase.phrase.description
            )
        } else {
            spellingDiff = AnnotatedString(pendingPhrase.phrase.spelling)
            descriptionDiff = AnnotatedString(pendingPhrase.phrase.description)
        }
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
                .padding(bottom = 16.dp)
        ) {
            Box {
                IconButton(
                    onClick = { compareDiff = !compareDiff },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = "compare"
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()
                ) {

                    Text(
                        text = pendingPhrase.phrase.phrase,
                        fontSize = 39.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = spellingDiff,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = descriptionDiff,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    )

                    Row {
                        ReviewStatus.entries.forEach { status ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (status == selectedStatus) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else Color.Unspecified
                                    )
                                    .padding(12.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            selectedStatus = status
                                        }
                                    )
                                    .weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = status.localizedName(),
                                        color = if (status == selectedStatus) {
                                            MaterialTheme.colorScheme.primary
                                        } else Color.Unspecified,
                                        fontWeight = if (status == selectedStatus) {
                                            FontWeight.W600
                                        } else FontWeight.W500,
                                        fontSize = 16.sp
                                    )
                                    RadioButton(
                                        selected = status == selectedStatus,
                                        onClick = {
                                            selectedStatus = status
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                onSave(selectedStatus)
                                onDismiss()
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.save_changes))
                        }

                        ElevatedButton(
                            onClick = onDismiss,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                }
            }
        }
    }
}

fun generateAnnotatedDiff(oldText: String, newText: String): AnnotatedString {
    val originalChars = oldText.toList()
    val newChars = newText.toList()
    val patch = diff(originalChars, newChars)

    return buildAnnotatedString {
        var lastOriginalIndex = 0

        patch.deltas.sortedBy { it.source.position }.forEach { delta ->
            if (delta.source.position > lastOriginalIndex) {
                append(oldText.substring(lastOriginalIndex, delta.source.position))
            }
            when (delta.type) {
                DeltaType.DELETE -> {
                    val deletedSegment = delta.source.lines.joinToString("")
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFFB00020),
                            background = Color(0xFFFFCDD2),
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    append(deletedSegment)
                    pop()
                }
                DeltaType.INSERT -> {
                    val insertedSegment = delta.target.lines.joinToString("")
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFF006400),
                            background = Color(0xFFC8E6C9),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    append(insertedSegment)
                    pop()
                }
                DeltaType.CHANGE -> {
                    val oldSegment = delta.source.lines.joinToString("")
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFFB00020),
                            background = Color(0xFFFFCDD2),
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    append(oldSegment)
                    pop()

                    val newSegment = delta.target.lines.joinToString("")
                    pushStyle(
                        SpanStyle(
                            color = Color(0xFF006400),
                            background = Color(0xFFC8E6C9),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    append(newSegment)
                    pop()
                }
                else -> {}
            }

            lastOriginalIndex = delta.source.position + delta.source.lines.size
        }

        if (lastOriginalIndex < oldText.length) {
            append(oldText.substring(lastOriginalIndex))
        }
    }
}