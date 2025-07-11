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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.BookItem
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.components.ChapterGrid
import org.bibletranslationtools.glossary.ui.screenmodel.HomeEvent
import org.bibletranslationtools.glossary.ui.screenmodel.WorkbookScreenModel

class BrowseScreen() : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<WorkbookScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        var firstLoaded by rememberSaveable { mutableStateOf(false) }

        var expandedBookIndex by rememberSaveable {
            mutableIntStateOf(state.books.indexOf(state.activeBook))
        }

        var selectedBook by rememberStringSetting(
            Settings.BOOK.name,
            "mat"
        )
        var selectedChapter by rememberIntSetting(
            Settings.CHAPTER.name,
            1
        )

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
                itemsIndexed(state.books) { index, book ->
                    BookItem(
                        book = book,
                        isExpanded = expandedBookIndex == index,
                        onToggle = {
                            expandedBookIndex = if (expandedBookIndex == index) -1 else index
                        }
                    )
                    if (expandedBookIndex == index) {
                        ChapterGrid(chapters = book.chapters.size) { chapter ->
                            if (state.activeBook != book) {
                                selectedBook = book.slug
                            }
                            if (state.activeChapter?.number != chapter) {
                                selectedChapter = chapter
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