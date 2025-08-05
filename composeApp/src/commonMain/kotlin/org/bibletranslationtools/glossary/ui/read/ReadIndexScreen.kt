@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package org.bibletranslationtools.glossary.ui.read

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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
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
import org.jetbrains.compose.resources.stringResource

private const val TITLE_GAP = 300

@OptIn(InternalTextApi::class)
@Composable
fun ReadIndexScreen(component: ReadIndexComponent) {
    val model by component.model.subscribeAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var activeBookSlug by rememberStringSetting(
        Settings.BOOK,
        "mat"
    )
    var activeChapterNum by rememberIntSetting(
        Settings.CHAPTER,
        1
    )

    val title by remember(model.activeBook, model.activeChapter) {
        derivedStateOf {
            model.activeBook?.let { book ->
                model.activeChapter?.let { chapter ->
                    "${book.title} ${chapter.number}"
                }
            } ?: ""
        }
    }

    var selectedText by remember { mutableStateOf("") }
    val isLoading by remember {
        derivedStateOf {
            model.activeBook == null
                    || model.activeChapter == null
        }
    }

    val bookChapterChanged by remember {
        derivedStateOf {
            val bookChanged = model.activeBook != null
                    && model.activeBook?.slug != activeBookSlug
            val chapterChanged = model.activeChapter != null
                    && model.activeChapter?.number != activeChapterNum
            bookChanged || chapterChanged
        }
    }

    var versePosition by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        component.setTopBar(null)
        if (model.currentRef == null) {
            component.loadRef(
                RefOption(
                    book = activeBookSlug,
                    chapter = activeChapterNum
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            component.clearRef()
        }
    }

    LaunchedEffect(bookChapterChanged) {
        if (bookChapterChanged) {
            model.activeBook?.let { book ->
                if (book.slug != activeBookSlug) {
                    activeBookSlug = book.slug
                }
            }
            model.activeChapter?.let { chapter ->
                if (chapter.number != activeChapterNum) {
                    activeChapterNum = chapter.number
                }
            }
            if (model.currentRef == null) {
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
            }
        }
    }

    LaunchedEffect(model.currentRef) {
        model.currentRef?.let { ref ->
            component.navigateBookChapter(
                bookSlug = ref.book,
                chapter = ref.chapter
            )
        }
    }

    LaunchedEffect(bookChapterChanged, model.currentRef) {
        val ref = model.currentRef ?: return@LaunchedEffect

        if (model.activeBook?.slug == ref.book
            && model.activeChapter?.number == ref.chapter
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
                            && model.currentRef != null) {
                            component.clearRef()
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

                model.activeChapter?.let { chapter ->
                    SelectableText(
                        chapter = chapter,
                        phrases = model.chapterPhrases,
                        selectedText = selectedText,
                        currentVerse = model.currentRef?.verse,
                        onSelectedTextChanged = { selectedText = it },
                        onSaveSelection = { component.onEditPhraseSelected(it) },
                        onPhraseClick = { phrase, verse ->
                            component.onPhraseClick(phrase, verse)
                        },
                        onVersePosition = { versePosition = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChapterNavigation(
                title = title,
                onBrowse = {
                    model.activeBook?.let { book ->
                        model.activeChapter?.let { chapter ->
                            component.onBrowseClick(book.slug, chapter.number)
                        }
                    }
                },
                onPrevClick = {
                    component.prevChapter()
                },
                onNextClick = {
                    component.nextChapter()
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