@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.book
import glossary.composeapp.generated.resources.loading
import glossary.composeapp.generated.resources.save
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.ChapterNavigation
import org.bibletranslationtools.glossary.ui.components.SelectableText
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.screenmodel.HomeEvent
import org.bibletranslationtools.glossary.ui.screenmodel.NavigationResult
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class ReadScreen : Screen {

    @OptIn(InternalTextApi::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ReadScreenModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        val navigator = LocalRootNavigator.currentOrThrow
        val snackBarHostState = LocalSnackBarHostState.currentOrThrow
        val scrollState = rememberScrollState()

        val selectedResource by rememberStringSetting(
            Settings.RESOURCE.name,
            "en_ulb"
        )
        var selectedBook by rememberStringSetting(
            Settings.BOOK.name,
            "mat"
        )
        var selectedChapter by rememberIntSetting(
            Settings.CHAPTER.name,
            1
        )
        var selectedGlossary by rememberStringSettingOrNull(
            Settings.GLOSSARY.name
        )

        val title = state.activeBook?.let { book ->
            state.activeChapter?.let { chapter ->
                "${book.title} ${chapter.number}"
            }
        } ?: stringResource(Res.string.loading)

        var selectedText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viewModel.onEvent(
                HomeEvent.InitLoad(
                    selectedResource,
                    selectedBook,
                    selectedChapter,
                    selectedGlossary
                )
            )
        }

        LaunchedEffect(event) {
            when (event) {
                is HomeEvent.OnNavigation -> {
                    val result = (event as HomeEvent.OnNavigation).result
                    when (result) {
                        is NavigationResult.ChapterChanged -> {
                            selectedChapter = result.chapter
                            scrollState.animateScrollTo(0)
                        }
                        is NavigationResult.BookChanged -> {
                            selectedBook = result.book
                            selectedChapter = result.chapter
                            scrollState.animateScrollTo(0)
                        }
                        else -> {}
                    }
                }
                is HomeEvent.SavePhrase -> {
                    val phrase = (event as HomeEvent.SavePhrase).phrase
                    navigator.push(
                        EditPhraseScreen(phrase, state.activeResource!!)
                    )
                }
                else -> {}
            }
        }

        LaunchedEffect(selectedText) {
            if (selectedText.isNotBlank()) {
                val result = snackBarHostState.showSnackbar(
                    message = selectedText,
                    actionLabel = getString(Res.string.save),
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onEvent(HomeEvent.OnSavePhrase(selectedText))
                }
                selectedText = ""
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        stringBuilder.append("${verse.number}. ${verse.text} ")
                    }
                    stringBuilder.toString()
                } ?: stringResource(Res.string.loading)

                SelectableText(
                    text = chapterText,
                    phrases = state.chapterPhrases,
                    selectedText = selectedText,
                    onSelectedTextChanged = {
                        selectedText = it
                    },
                    onPhraseClick = {
                        navigator.push(
                            ViewPhraseScreen(it, state.activeResource!!)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChapterNavigation(
                title = title,
                onBrowse = {
                    navigator.push(
                        BrowseScreen(
                            state.activeResource?.books,
                            state.activeBook,
                            state.activeChapter
                        ) { chapter, book ->
                            book?.let {
                                viewModel.onEvent(
                                    HomeEvent.NavigateBook(it, chapter)
                                )
                            } ?: run {
                                viewModel.onEvent(HomeEvent.NavigateChapter(chapter))
                            }
                        }
                    )
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