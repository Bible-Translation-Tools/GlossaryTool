@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package org.bibletranslationtools.glossary.ui.read

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.loading
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.ChapterNavigation
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.components.SelectableText
import org.bibletranslationtools.glossary.ui.data.FontFamilySetting
import org.bibletranslationtools.glossary.ui.data.FontSizeSetting
import org.bibletranslationtools.glossary.ui.data.LineHeightSetting
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(InternalTextApi::class)
@Composable
fun ReadIndexScreen(component: ReadIndexComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val resourceState by appStateStore.resourceStateHolder.state
        .collectAsStateWithLifecycle()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()

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
    var savedFontFamily by rememberStringSetting(
        Settings.FONT_FAMILY,
        "SansSerif"
    )
    var savedFontSize by rememberStringSetting(
        Settings.FONT_SIZE,
        "medium"
    )
    var savedLineHeight by rememberStringSetting(
        Settings.LINE_HEIGHT,
        "default"
    )

    val fontFamily by remember(savedFontFamily) {
        mutableStateOf(
            FontFamilySetting.of(savedFontFamily).value
        )
    }
    val fontSize by remember(savedFontSize) {
        mutableStateOf(
            FontSizeSetting.of(savedFontSize).value
        )
    }
    val lineHeight by remember(savedLineHeight) {
        mutableStateOf(
            LineHeightSetting.of(savedLineHeight).value
        )
    }

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

    LaunchedEffect(Unit) {
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

    LaunchedEffect(glossaryState.glossary) {
        component.reloadChapter()
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

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 36.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 36.dp)
                        ) {
                            IconButton(
                                onClick = component::openSettings
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "settings",
                                    tint = if (model.settingsDrawerOpen) {
                                        MaterialTheme.colorScheme.primary
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            fontSize = fontSize.times(2.25)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        model.activeChapter?.let { chapter ->
                            SelectableText(
                                chapter = chapter,
                                phrases = model.chapterPhrases,
                                selectedText = selectedText,
                                currentVerse = model.currentRef?.verse,
                                fontFamily = fontFamily,
                                fontSize = fontSize,
                                lineHeight = lineHeight,
                                onSelectedTextChanged = { selectedText = it },
                                onSaveSelection = { component.onPhraseSelected(it) },
                                onPhraseClick = { phrase, verse ->
                                    component.onPhraseClick(phrase, verse)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChapterNavigation(
                            title = title,
                            onBrowse = {
                                model.activeBook?.let { book ->
                                    model.activeChapter?.let { chapter ->
                                        component.onBrowseClick(book.slug, chapter.number)
                                    }
                                }
                            },
                            onPrevClick = component::prevChapter,
                            onNextClick = component::nextChapter,
                            modifier = Modifier.weight(1f)
                        )

                        Box {
                            IconButton(
                                onClick = component::openKeyTerms
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ListAlt,
                                    contentDescription = "glossary",
                                    tint = if (model.keyTermsDrawerOpen) {
                                        MaterialTheme.colorScheme.primary
                                    } else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (glossaryState.glossary != null) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = "has glossary",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                        .offset(x = -(14).dp, y = (14).dp)
                                        .border(1.dp, Color.White, CircleShape)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
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

    model.phraseDetails?.let { phraseDetails ->
        resourceState.resource?.let { resource ->
            PhraseDetailsBar(
                details = phraseDetails,
                resource = resource,
                fontFamily = fontFamily,
                onNavPhrase = { component.navigatePhrase(it) },
                onViewDetails = { phrase ->
                    component.onViewPhraseClick(phrase)
                },
                onDismiss = {
                    component.clearPhraseDetails()
                }
            )
        }
    }
}