package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.Utils.randomCode
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.data.toData
import org.bibletranslationtools.glossary.data.toEntity
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.PhraseDataSource
import org.bibletranslationtools.glossary.domain.RefDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSource

sealed class NavigationResult {
    object NoChange : NavigationResult()
    data class ChapterChanged(val chapter: Int) : NavigationResult()
    data class BookChanged(val book: String, val chapter: Int) : NavigationResult()
}

data class HomeState(
    val isLoading: Boolean = false,
    val activeResource: Resource? = null,
    val activeBook: Workbook? = null,
    val activeChapter: Chapter? = null,
    val activeGlossary: Glossary? = null,
    val chapterPhrases: List<Phrase> = emptyList()
)

sealed class HomeEvent {
    data object Idle : HomeEvent()
    data class InitLoad(
        val resource: String,
        val book: String,
        val chapter: Int,
        val glossary: String?
    ) : HomeEvent()
    data class LoadGlossary(val code: String) : HomeEvent()
    data class LoadResource(val resource: String, val book: String, val chapter: Int) : HomeEvent()
    data class NavigateBook(val book: String, val chapter: Int) : HomeEvent()
    data class NavigateChapter(val chapter: Int) : HomeEvent()
    data object NextChapter : HomeEvent()
    data object PrevChapter : HomeEvent()
    data class OnNavigation(val result: NavigationResult): HomeEvent()
    data class OnSavePhrase(val phrase: String): HomeEvent()
    data class SavePhrase(val phrase: Phrase): HomeEvent()
}

class ReadScreenModel(
    private val glossaryDataSource: GlossaryDataSource,
    private val workbookDataSource: WorkbookDataSource,
    private val phraseDataSource: PhraseDataSource,
    private val refDataSource: RefDataSource
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
            is HomeEvent.InitLoad -> initLoad(
                event.resource,
                event.book,
                event.chapter,
                event.glossary
            )
            is HomeEvent.NavigateBook -> {
                val result = navigateBook(event.book, event.chapter)
                screenModelScope.launch {
                    _event.send(HomeEvent.OnNavigation(result))
                }
            }
            is HomeEvent.NavigateChapter -> {
                val result = navigateChapter(event.chapter)
                screenModelScope.launch {
                    _event.send(HomeEvent.OnNavigation(result))
                }
            }
            is HomeEvent.NextChapter -> nextChapter()
            is HomeEvent.PrevChapter -> prevChapter()
            is HomeEvent.OnSavePhrase -> onSavePhrase(event.phrase)
            else -> resetChannel()
        }
    }

    private fun initLoad(
        resource: String,
        bookSlug: String,
        chapter: Int,
        glossary: String?
    ) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            loadGlossary(glossary)
            loadResource(resource, bookSlug, chapter)

            _state.value = _state.value.copy(isLoading = false)
        }
    }


    private suspend fun loadResource(resource: String, bookSlug: String, chapter: Int) {
        if (_state.value.activeResource?.slug != resource) {
            val books = withContext(Dispatchers.IO) {
                workbookDataSource.read(resource)
            }
            val book = books.find { it.slug == bookSlug } ?: books.firstOrNull()
            book?.let { selectedBook ->
                val chapter = selectedBook.chapters.find { it.number == chapter }
                    ?: selectedBook.chapters.firstOrNull()

                chapter?.let { selectedChapter ->
                    _state.value = _state.value.copy(
                        activeResource = Resource(resource, books),
                        activeBook = selectedBook,
                        activeChapter = selectedChapter
                    )
                    loadChapterPhrases()
                }
            }
        }
    }

    private suspend fun loadGlossary(code: String?) {
        code?.let {
            val glossary = withContext(Dispatchers.IO) {
                glossaryDataSource.getByCode(it)
            }
            if (glossary != null) {
                val id = glossary.id
                _state.value = _state.value.copy(
                    activeGlossary = glossary.toData {
                        runBlocking {
                            loadPhrases(id)
                        }
                    }
                )
            }
        }
    }

    private fun nextChapter() {
        _state.value.activeChapter?.let { chapter ->
            val current = chapter.number
            val next = current + 1
            val result = navigateChapter(next)
            if (result is NavigationResult.NoChange) {
                nextBook()
            } else {
                screenModelScope.launch {
                    _event.send(HomeEvent.OnNavigation(result))
                }
            }
        }
    }

    private fun prevChapter() {
        _state.value.activeChapter?.let { chapter ->
            val current = chapter.number
            val prev = current - 1
            val result = navigateChapter(prev)
            if (result is NavigationResult.NoChange) {
                prevBook()
            } else {
                screenModelScope.launch {
                    _event.send(HomeEvent.OnNavigation(result))
                }
            }
        }
    }

    private fun navigateChapter(chapter: Int): NavigationResult {
        _state.value.activeBook?.let { book ->
            book.chapters.singleOrNull { it.number == chapter }?.let {
                _state.value = _state.value.copy(activeChapter = it)
                loadChapterPhrases()
                return NavigationResult.ChapterChanged(chapter)
            }
        }
        return NavigationResult.NoChange
    }

    private fun nextBook() {
        _state.value.activeBook?.let { book ->
            _state.value.activeResource?.books?.let { books ->
                val index = books.indexOf(book) + 1
                _state.value.activeResource?.books?.getOrNull(index)?.let { book ->
                    book.chapters.firstOrNull()?.let { chapter ->
                        val result = navigateBook(book, chapter)
                        screenModelScope.launch {
                            _event.send(HomeEvent.OnNavigation(result))
                        }
                    }
                }
            }
        }
    }

    private fun prevBook() {
        _state.value.activeBook?.let { book ->
            _state.value.activeResource?.books?.let { books ->
                val index = books.indexOf(book) - 1
                _state.value.activeResource?.books?.getOrNull(index)?.let { book ->
                    book.chapters.lastOrNull()?.let { chapter ->
                        val result = navigateBook(book, chapter)
                        screenModelScope.launch {
                            _event.send(HomeEvent.OnNavigation(result))
                        }
                    }
                }
            }
        }
    }

    private fun navigateBook(bookSlug: String, chapter: Int): NavigationResult {
        _state.value.activeResource?.books?.find { it.slug == bookSlug }?.let { book ->
            book.chapters.find { it.number == chapter }?.let { chapter ->
                return navigateBook(book, chapter)
            }
        }
        return NavigationResult.NoChange
    }

    private fun navigateBook(book: Workbook, chapter: Chapter): NavigationResult {
        _state.value = _state.value.copy(
            activeBook = book,
            activeChapter = chapter
        )
        loadChapterPhrases()
        return NavigationResult.BookChanged(
            book = book.slug,
            chapter = chapter.number
        )
    }

    private fun loadChapterPhrases() {
        screenModelScope.launch {
            _state.value.activeGlossary?.let { glossary ->
                _state.value.activeResource?.let { resource ->
                    _state.value.activeBook?.let { book ->
                        _state.value.activeChapter?.let { chapter ->
                            val phrases = glossary.phrases.filter { phrase ->
                                phrase.refs.any { ref ->
                                    ref.resource == resource.slug
                                            && ref.book == book.slug
                                            && ref.chapter == chapter.number.toString()
                                }
                            }
                            _state.value = _state.value.copy(chapterPhrases = phrases)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadPhrases(glossaryId: String): List<Phrase> {
        return phraseDataSource.getByGlossary(glossaryId)
            .map {
                it.toData {
                    runBlocking { loadRefs(it.id) }
                }
            }
    }

    private suspend fun loadRefs(phraseId: String): List<Ref> {
        return refDataSource.getByPhrase(phraseId)
            .map { it.toData() }
    }

    private fun onSavePhrase(phrase: String) {
        screenModelScope.launch {
            (_state.value.activeGlossary ?: run {
                val code = randomCode()
                val glossary = Glossary(code, "user")
                val id = glossaryDataSource.insert(glossary.toEntity())
                id?.let { glossary.copy(id = id) }
            })?.let { glossary ->
                val phrase = phraseDataSource.getByPhrase(phrase, glossary.id!!)
                    ?.toData { emptyList() } ?: Phrase(
                    phrase = phrase,
                    glossaryId = glossary.id
                )
                _event.send(HomeEvent.SavePhrase(phrase))
            }
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(HomeEvent.Idle)
        }
    }
}