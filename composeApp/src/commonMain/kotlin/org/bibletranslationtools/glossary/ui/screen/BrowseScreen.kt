package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.browse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.components.BookItem
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.components.ChapterGrid
import org.jetbrains.compose.resources.stringResource

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
                BrowseTopBar(
                    title = stringResource(Res.string.browse),
                    actions = {
                        IconButton(onClick = { /* Handle search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        Button(
                            onClick = { /* Handle language change */ },
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
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