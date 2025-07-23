package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChapterGrid(
    chapters: Int,
    activeChapter: Int?,
    bringIntoViewRequester: BringIntoViewRequester,
    onChapterClick: (Int) -> Unit
) {
    val chapterNumbers = (1..chapters).toList()
    val chunkedChapters = chapterNumbers.chunked(5)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedChapters.forEach { rowChapters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowChapters.forEach { chapter ->
                    val isActive = chapter == activeChapter
                    var buttonModifier = Modifier.weight(1f)

                    if (isActive) {
                        buttonModifier = buttonModifier
                            .bringIntoViewRequester(bringIntoViewRequester)
                    }

                    ChapterButton(
                        chapter = chapter,
                        isActive = chapter == activeChapter,
                        onClick = { onChapterClick(chapter) },
                        modifier = buttonModifier
                    )
                }
                val remainingSpace = 5 - rowChapters.size
                if (remainingSpace > 0) {
                    Spacer(modifier = Modifier.weight(remainingSpace.toFloat()))
                }
            }
        }
    }
}