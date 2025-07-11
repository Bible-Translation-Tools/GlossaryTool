package org.bibletranslationtools.glossary.ui.viewmodel

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
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSource

data class HomeState(
    val books: List<Workbook> = emptyList(),
    val activeBook: Workbook? = null
)

sealed class HomeEvent {
    data object Idle : HomeEvent()
    data object LoadBooks : HomeEvent()
    data class LoadBook(val book: Workbook) : HomeEvent()
}

class HomeViewModel(
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
            is HomeEvent.LoadBooks -> loadBooks()
            is HomeEvent.LoadBook -> loadBook(event.book)
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

    private fun loadBooks() {
        screenModelScope.launch {
            val books = withContext(Dispatchers.IO) {
                workbookDataSource.read("en", "ulb")
            }
            updateBooks(books)
            println("Loaded ${books.size} books.")
        }
    }

    private fun loadBook(book: Workbook) {
        screenModelScope.launch {
            updateActiveBook(book)
            println(book.sort)
            println(book.slug)
            println(book.title)
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

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(HomeEvent.Idle)
        }
    }
}