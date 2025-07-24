@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import glossary.composeapp.generated.resources.loading
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.ChapterNavigation
import org.bibletranslationtools.glossary.ui.components.SelectableText
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.screenmodel.NavigationResult
import org.bibletranslationtools.glossary.ui.screenmodel.PhraseDetails
import org.bibletranslationtools.glossary.ui.screenmodel.ReadEvent
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedEvent
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class ReadScreen : Screen {

    @OptIn(InternalTextApi::class)
    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()
        val resourceState by appStateStore.resourceStateHolder.resourceState
            .collectAsStateWithLifecycle()

        val screenModel = koinScreenModel<ReadScreenModel>()
        val tabbedScreenModel = koinScreenModel<TabbedScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()
        val event by screenModel.event.collectAsStateWithLifecycle(ReadEvent.Idle)

        val navigator = LocalRootNavigator.currentOrThrow
        val scrollState = rememberScrollState()

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

        val title by remember(state.activeBook, state.activeChapter) {
            derivedStateOf {
                state.activeBook?.let { book ->
                    state.activeChapter?.let { chapter ->
                        "${book.title} ${chapter.number}"
                    }
                } ?: ""
            }
        }

        var selectedText by remember { mutableStateOf("") }
        var textIsReady by remember { mutableStateOf(false) }
        val isLoading by remember {
            derivedStateOf {
                state.activeBook == null
                        || state.activeChapter == null
                        || !textIsReady
            }
        }

        LaunchedEffect(Unit) {
            screenModel.onEvent(
                ReadEvent.InitLoad(
                    selectedBook,
                    selectedChapter,
                    selectedGlossary
                )
            )
        }

        LaunchedEffect(event) {
            when (event) {
                is ReadEvent.OnNavigation -> {
                    val result = (event as ReadEvent.OnNavigation).result
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
                is ReadEvent.SavePhrase -> {
                    val phrase = (event as ReadEvent.SavePhrase).phrase
                    navigator.push(
                        EditPhraseScreen(phrase)
                    )
                }
                is ReadEvent.GlossaryChanged -> {
                    selectedGlossary = (event as ReadEvent.GlossaryChanged).glossary.code
                }
                else -> {}
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 8.dp
            )
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    state.activeChapter?.let { chapter ->
                        SelectableText(
                            chapter = chapter,
                            phrases = state.chapterPhrases,
                            selectedText = selectedText,
                            onSelectedTextChanged = { selectedText = it },
                            onSaveSelection = {
                                screenModel.onEvent(ReadEvent.OnSavePhrase(it))
                            },
                            onPhraseClick = { phrase, verse ->
                                tabbedScreenModel.onEvent(
                                    TabbedEvent.LoadPhrase(
                                        PhraseDetails(
                                            phrase = phrase,
                                            phrases = state.chapterPhrases,
                                            resource = resourceState.resource!!,
                                            book = state.activeBook!!,
                                            chapter = state.activeChapter!!,
                                            verse = verse
                                        )
                                    )
                                )
                            },
                            onTextReady = { textIsReady = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ChapterNavigation(
                    title = title,
                    onBrowse = {
                        resourceState.resource?.let { resource ->
                            state.activeBook?.let { book ->
                                state.activeChapter?.let { chapter ->
                                    textIsReady = false
                                    navigator.push(
                                        BrowseScreen(
                                            resource.books,
                                            book,
                                            chapter
                                        ) { chapter, book ->
                                            book?.let {
                                                screenModel.onEvent(
                                                    ReadEvent.NavigateBookChapter(it, chapter)
                                                )
                                            } ?: run {
                                                screenModel.onEvent(
                                                    ReadEvent.NavigateChapter(chapter)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    onPrevClick = {
                        textIsReady = false
                        screenModel.onEvent(ReadEvent.PrevChapter)
                    },
                    onNextClick = {
                        textIsReady = false
                        screenModel.onEvent(ReadEvent.NextChapter)
                    }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(Res.string.loading),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}