package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.state.AppStateStore

data class HomeState(
    val isLoading: Boolean = false,
    val activeBook: Workbook? = null,
    val activeChapter: Chapter? = null,
    val chapterPhrases: List<Phrase> = emptyList()
)

enum class NavDir(val incr: Int) {
    NEXT(1),
    PREV(-1)
}

class ReadScreenModel(appStateStore: AppStateStore) : ScreenModel {

    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val resourceState = appStateStore.resourceStateHolder.resourceState
    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState

    fun initLoad(
        bookSlug: String,
        chapter: Int,
        currentRef: RefOption?
    ) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            withContext(Dispatchers.IO) {
                if (_state.value.activeBook?.slug != bookSlug
                    && _state.value.activeChapter?.number != chapter) {
                    loadResource(bookSlug, chapter)
                }
                if (currentRef == null) {
                    loadChapterPhrases()
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadResource(bookSlug: String, chapter: Int) {
        resourceState.value.resource?.let { resource ->
            val book = resource.books.find { it.slug == bookSlug }
                ?: resource.books.firstOrNull()

            book?.let { selectedBook ->
                val chapter = selectedBook.chapters.find { it.number == chapter }
                    ?: selectedBook.chapters.firstOrNull()

                chapter?.let { selectedChapter ->
                    _state.value = _state.value.copy(
                        activeBook = selectedBook,
                        activeChapter = selectedChapter
                    )
                }
            }
        }
    }

    fun nextChapter() {
        screenModelScope.launch {
            navigateChapter(NavDir.NEXT)
        }
    }

    fun prevChapter() {
        screenModelScope.launch {
            navigateChapter(NavDir.PREV)
        }
    }

    private fun navigateChapter(dir: NavDir) {
        _state.value.activeChapter?.let { chapter ->
            val current = chapter.number
            val chapter = current + dir.incr

            val found = _state.value.activeBook?.let { book ->
                book.chapters.singleOrNull { it.number == chapter }?.let { chapter ->
                    navigateBookChapter(book, chapter)
                    true
                } ?: false
            } ?: false

            if (!found) {
                navigateBook(dir)
            }
        }
    }

    private fun navigateBook(dir: NavDir) {
        resourceState.value.resource?.books?.let { books ->
            _state.value.activeBook?.let { book ->
                val index = books.indexOf(book) + dir.incr
                books.getOrNull(index)?.let { book ->
                    val chapter = if (dir == NavDir.PREV) {
                        book.chapters.lastOrNull()
                    } else {
                        book.chapters.firstOrNull()
                    }
                    chapter?.let { chapter ->
                        navigateBookChapter(book, chapter)
                    }
                }
            }
        }
    }

    fun navigateBookChapter(bookSlug: String, chapter: Int) {
        resourceState.value.resource?.books
            ?.find { it.slug == bookSlug }?.let { book ->
                book.chapters.find { it.number == chapter }?.let { chapter ->
                    navigateBookChapter(book, chapter)
                }
            }
    }

    private fun navigateBookChapter(book: Workbook, chapter: Chapter) {
        _state.value = _state.value.copy(
            activeBook = book,
            activeChapter = chapter
        )
        loadChapterPhrases()
    }

    private fun loadChapterPhrases() {
        resourceState.value.resource?.let { res ->
            glossaryState.value.glossary?.let { glossary ->
                _state.value.activeBook?.let { book ->
                    _state.value.activeChapter?.let { chapter ->
                        val phrases = glossary.phrases.filter { phrase ->
                            phrase.refs.any { ref ->
                                ref.resource == res.slug
                                        && ref.book == book.slug
                                        && ref.chapter == chapter.number.toString()
                            }
                        }.sortedWith(
                            compareBy {
                                it.refs.first { ref ->
                                    ref.resource == res.slug
                                            && ref.book == book.slug
                                            && ref.chapter == chapter.number.toString()
                                }.verse.toIntOrNull() ?: Int.MAX_VALUE
                            }
                        )

                        _state.update { it.copy(chapterPhrases = phrases) }
                    }
                }
            }
        }
    }
}