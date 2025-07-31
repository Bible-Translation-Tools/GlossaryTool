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
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.state.AppStateStore

data class ReadState(
    val isLoading: Boolean = false,
    val activeBook: Workbook? = null,
    val activeChapter: Chapter? = null,
    val chapterPhrases: List<Phrase> = emptyList()
)

enum class NavDir(val incr: Int) {
    NEXT(1),
    PREV(-1)
}

class ReadScreenModel(
    appStateStore: AppStateStore,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(ReadState())
    val state: StateFlow<ReadState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReadState()
        )

    private val resourceState = appStateStore.resourceStateHolder.resourceState
    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState

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
        val book = _state.value.activeBook ?: return
        val chapter = _state.value.activeChapter ?: return

        val current = chapter.number
        val newChapter = current + dir.incr

        val found = book.chapters.singleOrNull { it.number == newChapter }
            ?.let { chapter ->
                navigateBookChapter(book, chapter)
                true
            } ?: false

        if (!found) {
            navigateBook(dir)
        }
    }

    private fun navigateBook(dir: NavDir) {
        val books = resourceState.value.resource?.books ?: return
        val book = _state.value.activeBook ?: return
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
        screenModelScope.launch {
            val resource = resourceState.value.resource ?: return@launch
            val glossary = glossaryState.value.glossary ?: return@launch
            val book = _state.value.activeBook ?: return@launch
            val chapter = _state.value.activeChapter ?: return@launch

            val phrases = withContext(Dispatchers.Default) {
                glossaryRepository.getPhrases(glossary.id)
                    .filter { it.resourceId == resource.id }
                    .mapNotNull { phrase ->
                        val relevantRef = glossaryRepository.getRefs(phrase.id)
                            .find { ref ->
                                ref.book == book.slug
                                        && ref.chapter == chapter.number.toString()
                            }
                        relevantRef?.let { phrase to it }
                    }
                    .sortedBy { (_, ref) ->
                        ref.verse.toIntOrNull() ?: Int.MAX_VALUE
                    }
                    .map { (phrase, _) -> phrase }
            }

            _state.update { it.copy(chapterPhrases = phrases) }
        }
    }
}