package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.components.BookItem
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.components.ChapterGrid

class BrowseScreen(
    private val books: List<Workbook>?,
    private val activeBook: Workbook?,
    private val activeChapter: Chapter?,
    private val onBack: (Int, String?) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        var firstLoaded by rememberSaveable { mutableStateOf(false) }

        var expandedBookIndex by rememberSaveable {
            mutableIntStateOf(books?.indexOf(activeBook) ?: -1)
        }

        LaunchedEffect(expandedBookIndex) {
            if (expandedBookIndex != -1) {
                coroutineScope.launch {
                    if (!firstLoaded) {
                        delay(300)
                        firstLoaded = true
                    }
                    lazyListState.animateScrollToItem(expandedBookIndex)
                }
            }
        }

        Scaffold(
            topBar = {
                BrowseTopBar {
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
                itemsIndexed(books ?: emptyList()) { index, book ->
                    BookItem(
                        book = book,
                        isExpanded = expandedBookIndex == index,
                        onToggle = {
                            expandedBookIndex = if (expandedBookIndex == index) -1 else index
                        }
                    )
                    if (expandedBookIndex == index) {
                        ChapterGrid(chapters = book.chapters.size) { chapter ->
                            if (activeBook != book) {
                                onBack(chapter, book.slug)
                            } else if (activeChapter?.number != chapter) {
                                onBack(chapter, null)
                            }
                            navigator.pop()
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}