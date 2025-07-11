package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.book
import kotlinx.coroutines.channels.actor
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.BottomNavBar
import org.bibletranslationtools.glossary.ui.components.ChapterNavigation
import org.bibletranslationtools.glossary.ui.screenmodel.HomeEvent
import org.bibletranslationtools.glossary.ui.screenmodel.WorkbookScreenModel
import org.jetbrains.compose.resources.painterResource

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<WorkbookScreenModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        val navigator = LocalNavigator.currentOrThrow

        val scrollState = rememberScrollState()

        val selectedResource by rememberStringSetting(
            Settings.RESOURCE.name,
            "en_ulb"
        )
        val selectedBook by rememberStringSetting(
            Settings.BOOK.name,
            "mat"
        )
        var selectedChapter by rememberIntSetting(
            Settings.CHAPTER.name,
            1
        )

        val title = state.activeBook?.let { book ->
            state.activeChapter?.let { chapter ->
                "${book.title} ${chapter.number}"
            }
        } ?: "Loading..."

        LaunchedEffect(selectedResource) {
            viewModel.onEvent(HomeEvent.LoadBooks(selectedResource))
        }

        LaunchedEffect(state.activeChapter) {
            state.activeChapter?.let { chapter ->
                selectedChapter = chapter.number
                scrollState.animateScrollTo(0)
            }
        }

        LaunchedEffect(event) {
            when (event) {
                is HomeEvent.BooksLoaded -> {
                    viewModel.onEvent(HomeEvent.LoadBook(selectedBook))
                }
                is HomeEvent.BookLoaded -> {
                    state.activeBook?.let { book ->
                        book.chapters.singleOrNull { it.number == selectedChapter }?.let {
                            viewModel.onEvent(HomeEvent.LoadChapter(it.number))
                        }
                    }
                }
                else -> {}
            }
        }

        Scaffold(
            bottomBar = { BottomNavBar() }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.book),
                        contentDescription = "book",
                        modifier = Modifier
                            .height(150.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val chapterText = state.activeChapter?.let { chapter ->
                        val stringBuilder = StringBuilder()
                        chapter.verses.forEach { verse ->
                            stringBuilder.append("${verse.name}. ${verse.text}\n")
                        }
                        stringBuilder.append("\n")
                        stringBuilder.toString()
                    } ?: "Loading..."

                    Text(
                        text = chapterText,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                ChapterNavigation(
                    title = title,
                    onBookChange = {
                        navigator.push(BrowseScreen())
                    },
                    onPrevClick = {
                        viewModel.onEvent(HomeEvent.PrevChapter)
                    },
                    onNextClick = {
                        viewModel.onEvent(HomeEvent.NextChapter)
                    }
                )
            }
        }
    }
}