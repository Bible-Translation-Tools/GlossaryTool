package org.bibletranslationtools.glossary.ui.read

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.browse
import kotlinx.coroutines.delay
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.ui.components.BookItem
import org.bibletranslationtools.glossary.ui.components.ChapterGrid
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(component: BrowseComponent) {
    val model by component.model.subscribeAsState()

    val lazyListState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var expandedBookIndex by rememberSaveable {
        mutableIntStateOf(model.books.indexOf(model.book))
    }

    var filteredBooks by remember { mutableStateOf(model.books) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchFocused by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        component.setTopBar {
            TopAppBar(
                title = stringResource(Res.string.browse),
                actions = {
                    SearchField(
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it },
                        onFocusChange = { searchFocused = it },
                        colors = CustomTextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                            .height(48.dp)
                    )

                    if (!searchFocused) {
                        Button(
                            onClick = { /* Handle language change */ },
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ENG")
                        }
                    }
                }
            ) {
                component.onBackClick()
            }
        }

        val initialBookIndex = model.books.indexOf(model.book)
        expandedBookIndex = initialBookIndex
        delay(500)
        bringIntoViewRequester.bringIntoView()
    }

    LaunchedEffect(expandedBookIndex) {
        if (expandedBookIndex != -1) {
            delay(200)
            val visibleItem = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index == expandedBookIndex
            }
            if (visibleItem != null) {
                lazyListState.animateScrollBy(
                    value = visibleItem.offset.toFloat(),
                    animationSpec = tween(500)
                )
            } else {
                lazyListState.animateScrollToItem(expandedBookIndex)
            }
        }
    }

    LaunchedEffect(searchQuery) {
        filteredBooks = model.books.filter { book ->
            book.title.contains(searchQuery, ignoreCase = true)
                    || book.slug.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = lazyListState
    ) {
        itemsIndexed(filteredBooks) { index, book ->
            BookItem(
                book = book,
                isExpanded = expandedBookIndex == index,
                onToggle = {
                    expandedBookIndex = if (expandedBookIndex == index) -1 else index
                },
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = expandedBookIndex == index,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                ChapterGrid(
                    chapters = book.chapters.size,
                    activeChapter = if (book == model.book) model.chapter?.number else null,
                    bringIntoViewRequester = bringIntoViewRequester,
                    modifier = Modifier.fillMaxWidth(),
                    onChapterClick = { chapter ->
                        component.onRefClick(
                            RefOption(
                                book = book.slug,
                                chapter = chapter
                            )
                        )
                    }
                )
            }
            HorizontalDivider()
        }
    }
}