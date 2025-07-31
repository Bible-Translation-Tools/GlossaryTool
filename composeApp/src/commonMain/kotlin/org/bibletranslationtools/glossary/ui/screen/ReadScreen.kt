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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.loading
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.ChapterNavigation
import org.bibletranslationtools.glossary.ui.components.SelectableText
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SharedScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val TITLE_GAP = 300

class ReadScreen : Screen {

    @OptIn(InternalTextApi::class)
    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()
        val resourceState by appStateStore.resourceStateHolder.resourceState
            .collectAsStateWithLifecycle()

        val navigator = LocalRootNavigator.currentOrThrow
        val screenModel = koinScreenModel<ReadScreenModel>()
        val sharedScreenModel = navigator.koinNavigatorScreenModel<SharedScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()
        val tabbedState by sharedScreenModel.state.collectAsStateWithLifecycle()

        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        var activeBookSlug by rememberStringSetting(
            Settings.BOOK.name,
            "mat"
        )
        var activeChapterNum by rememberIntSetting(
            Settings.CHAPTER.name,
            1
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
        val isLoading by remember {
            derivedStateOf {
                state.activeBook == null
                        || state.activeChapter == null
            }
        }

        val bookChapterChanged by remember {
            derivedStateOf {
                val bookChanged = state.activeBook != null
                        && state.activeBook?.slug != activeBookSlug
                val chapterChanged = state.activeChapter != null
                        && state.activeChapter?.number != activeChapterNum
                bookChanged || chapterChanged
            }
        }

        var versePosition by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            if (tabbedState.currentRef == null) {
                sharedScreenModel.loadRef(
                    RefOption(
                        book = activeBookSlug,
                        chapter = activeChapterNum
                    )
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                sharedScreenModel.clearRef()
            }
        }

        LaunchedEffect(bookChapterChanged) {
            if (bookChapterChanged) {
                state.activeBook?.let { book ->
                    if (book.slug != activeBookSlug) {
                        activeBookSlug = book.slug
                    }
                }
                state.activeChapter?.let { chapter ->
                    if (chapter.number != activeChapterNum) {
                        activeChapterNum = chapter.number
                    }
                }
                if (tabbedState.currentRef == null) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                }
            }
        }

        LaunchedEffect(tabbedState.currentRef) {
            tabbedState.currentRef?.let { ref ->
                screenModel.navigateBookChapter(
                    bookSlug = ref.book,
                    chapter = ref.chapter
                )
            }
        }

        LaunchedEffect(bookChapterChanged, tabbedState.currentRef) {
            val ref = tabbedState.currentRef ?: return@LaunchedEffect

            if (state.activeBook?.slug == ref.book
                && state.activeChapter?.number == ref.chapter
            ) {
                snapshotFlow { versePosition }
                    .drop(1)
                    .first()
                    .let { newPosition ->
                        scrollState.animateScrollTo(versePosition - TITLE_GAP)
                    }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press
                                && tabbedState.currentRef != null) {
                                sharedScreenModel.clearRef()
                            }
                        }
                    }
                }
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
                            currentVerse = tabbedState.currentRef?.verse,
                            onSelectedTextChanged = { selectedText = it },
                            onSaveSelection = {
                                navigator.push(EditPhraseScreen(it))
                            },
                            onPhraseClick = { phrase, verse ->
                                sharedScreenModel.loadPhrase(
                                    phrase = phrase,
                                    phrases = state.chapterPhrases,
                                    book = state.activeBook!!,
                                    chapter = state.activeChapter!!,
                                    verse = verse
                                )
                            },
                            onVersePosition = { versePosition = it }
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
                                    navigator.push(
                                        BrowseScreen(
                                            resource.books,
                                            book,
                                            chapter
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onPrevClick = {
                        screenModel.prevChapter()
                    },
                    onNextClick = {
                        screenModel.nextChapter()
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