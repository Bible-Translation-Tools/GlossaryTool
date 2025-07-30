package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.browse
import kotlinx.coroutines.delay
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.components.BookItem
import org.bibletranslationtools.glossary.ui.components.ChapterGrid
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.KeyboardAware
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.event.AppEvent
import org.bibletranslationtools.glossary.ui.event.EventBus
import org.jetbrains.compose.resources.stringResource

class BrowseScreen(
    private val books: List<Workbook>,
    private val activeBook: Workbook,
    private val activeChapter: Chapter
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val lazyListState = rememberLazyListState()
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        var expandedBookIndex by rememberSaveable {
            mutableIntStateOf(books.indexOf(activeBook))
        }

        var filteredBooks by remember { mutableStateOf(books) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchFocused by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val initialBookIndex = books.indexOf(activeBook)
            if (initialBookIndex != -1) {
                expandedBookIndex = initialBookIndex
                delay(300)
                bringIntoViewRequester.bringIntoView()
            }
        }

        LaunchedEffect(expandedBookIndex) {
            if (expandedBookIndex != -1) {
                lazyListState.animateScrollToItem(expandedBookIndex)
                bringIntoViewRequester.bringIntoView()
            }
        }

        LaunchedEffect(searchQuery) {
            filteredBooks = books.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true)
                        || book.slug.contains(searchQuery, ignoreCase = true)
            }
        }

        KeyboardAware {
            Scaffold(
                topBar = {
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
                        navigator.pop()
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                        if (expandedBookIndex == index) {
                            ChapterGrid(
                                chapters = book.chapters.size,
                                activeChapter = if (book == activeBook) activeChapter.number else null,
                                bringIntoViewRequester = bringIntoViewRequester,
                                modifier = Modifier.fillMaxWidth(),
                                onChapterClick = { chapter ->
                                    EventBus.events.trySend(
                                        AppEvent.OpenRef(RefOption(
                                            book = book.slug,
                                            chapter = chapter
                                        ))
                                    )
                                    navigator.popUntil { it is TabbedScreen }
                                }
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}