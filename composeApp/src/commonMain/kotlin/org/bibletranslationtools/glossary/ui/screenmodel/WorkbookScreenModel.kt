package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSource

data class HomeState(
    val books: List<Workbook> = emptyList(),
    val activeBook: Workbook? = null,
    val activeChapter: Chapter? = null
)

sealed class HomeEvent {
    data object Idle : HomeEvent()
    data class LoadBooks(val resource: String) : HomeEvent()
    data object BooksLoaded : HomeEvent()
    data class LoadBook(val book: String) : HomeEvent()
    data object BookLoaded : HomeEvent()
    data class LoadChapter(val chapter: Int) : HomeEvent()
    data object NextChapter : HomeEvent()
    data object PrevChapter : HomeEvent()
}

class WorkbookScreenModel(
    private val glossaryDataSource: GlossaryDataSource,
    private val workbookDataSource: WorkbookDataSource
) : ScreenModel {

    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val _event: Channel<HomeEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadBooks -> loadBooks(event.resource)
            is HomeEvent.LoadBook -> loadBook(event.book)
            is HomeEvent.LoadChapter -> loadChapter(event.chapter)
            is HomeEvent.NextChapter -> nextChapter()
            is HomeEvent.PrevChapter -> prevChapter()
            else -> resetChannel()
        }
    }

    fun insert() {
        screenModelScope.launch {
            val date = Clock.System.now()
            glossaryDataSource.insert("test", "test", date.epochSeconds)
        }
    }

    fun getAll() {
        screenModelScope.launch {
            glossaryDataSource.getAll().collectLatest { records ->
                records.forEach {
                    println(it.code)
                }
            }
        }
    }

    private fun loadBooks(resource: String) {
        screenModelScope.launch {
            val books = withContext(Dispatchers.IO) {
                workbookDataSource.read(resource)
            }
            updateBooks(books)
            _event.send(HomeEvent.BooksLoaded)
        }
    }

    private fun loadBook(book: String) {
        screenModelScope.launch {
            state.value.books.singleOrNull { it.slug == book }?.let {
                updateActiveBook(it)
                _event.send(HomeEvent.BookLoaded)
            }
        }
    }

    private fun loadChapter(chapter: Int) {
        screenModelScope.launch {
            navigateChapter(chapter)
        }
    }

    private fun nextChapter() {
        state.value.activeChapter?.let { chapter ->
            val current = chapter.number
            val next = current + 1
            navigateChapter(next)
        }
    }

    private fun prevChapter() {
        state.value.activeChapter?.let { chapter ->
            val current = chapter.number
            val prev = current - 1
            navigateChapter(prev)
        }
    }

    private fun navigateChapter(chapter: Int) {
        state.value.activeBook?.let { book ->
            book.chapters.singleOrNull { it.number == chapter }?.let {
                updateActiveChapter(it)
            }
        }
    }

    private fun updateBooks(books: List<Workbook>) {
        _state.update {
            it.copy(books = books)
        }
    }

    private fun updateActiveBook(book: Workbook) {
        _state.update {
            it.copy(activeBook = book)
        }
    }

    private fun updateActiveChapter(chapter: Chapter) {
        _state.update {
            it.copy(activeChapter = chapter)
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(HomeEvent.Idle)
        }
    }
}