package org.bibletranslationtools.glossary.ui.drawer.keyterms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
import org.bibletranslationtools.glossary.ui.components.ChapterVerse
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar

@Composable
fun ViewChapterScreen(component: ViewChapterComponent) {
    val model by component.model.subscribeAsState()

    val scrollState = rememberLazyListState()
    var viewHeight by remember { mutableStateOf(0.dp) }

    var focusOnVerse by remember { mutableStateOf(true) }

    LaunchedEffect(model.ref, model.verses) {
        val index = model.verses.indexOfFirst { it.number == model.ref?.verse }
        if (index != -1) {
            val offset = (-(viewHeight / 2).value).toInt()
            delay(500)
            scrollState.animateScrollToItem(index, offset)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopDrawerBar(
            title = model.phrase?.phrase ?: "",
            subTitle = model.ref?.let {
                "${it.book.uppercase()} ${it.chapter}"
            },
            onBackClick = component::navigateBack,
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Press && focusOnVerse) {
                                        focusOnVerse = false
                                    }
                                }
                            }
                        }
                ) {
                    if (model.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            viewHeight = maxHeight

                            LazyColumn(
                                state = scrollState,
                                modifier = Modifier.fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.medium
                                    )
                            ) {
                                items(model.verses) { verse ->
                                    model.phrase?.phrase?.let { phrase ->
                                        model.ref?.let { ref ->
                                            ChapterVerse(verse, ref, phrase, focusOnVerse)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
