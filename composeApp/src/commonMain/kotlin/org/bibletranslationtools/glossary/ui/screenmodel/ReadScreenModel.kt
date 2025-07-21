package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import org.bibletranslationtools.glossary.domain.GlossaryRepository
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

sealed class ReadEvent {
    data object Idle : ReadEvent()
    data class InitLoad(
        val resource: String,
        val book: String,
        val chapter: Int,
        val glossary: String?
    ) : ReadEvent()
    data class LoadGlossary(val code: String?) : ReadEvent()
    data class LoadResource(val resource: String, val book: String, val chapter: Int) : ReadEvent()
    data class NavigateBook(val book: String, val chapter: Int) : ReadEvent()
    data class NavigateChapter(val chapter: Int) : ReadEvent()
    data object NextChapter : ReadEvent()
    data object PrevChapter : ReadEvent()
    data class OnNavigation(val result: NavigationResult): ReadEvent()
    data class OnSavePhrase(val phrase: String): ReadEvent()
    data class GlossaryChanged(val glossary: Glossary): ReadEvent()
    data class SavePhrase(val phrase: Phrase): ReadEvent()
}

class ReadScreenModel(
    private val glossaryRepository: GlossaryRepository,
    private val workbookDataSource: WorkbookDataSource
) : ScreenModel {

    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    private val _event: Channel<ReadEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: ReadEvent) {
        when (event) {
            is ReadEvent.InitLoad -> initLoad(
                event.resource,
                event.book,
                event.chapter,
                event.glossary
            )
            is ReadEvent.NavigateBook -> screenModelScope.launch {
                val result = navigateBook(event.book, event.chapter)
                _event.send(ReadEvent.OnNavigation(result))
            }
            is ReadEvent.NavigateChapter -> screenModelScope.launch {
                val result = navigateChapter(event.chapter)
                _event.send(ReadEvent.OnNavigation(result))
            }
            is ReadEvent.NextChapter -> nextChapter()
            is ReadEvent.PrevChapter -> prevChapter()
            is ReadEvent.OnSavePhrase -> onSavePhrase(event.phrase)
            is ReadEvent.LoadGlossary -> screenModelScope.launch {
                loadGlossary(event.code)
            }
            is ReadEvent.LoadResource -> screenModelScope.launch {
                loadResource(
                    event.resource,
                    event.book,
                    event.chapter
                )
            }
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

            withContext(Dispatchers.IO) {
                loadGlossary(glossary)
                loadResource(resource, bookSlug, chapter)
                loadChapterPhrases()
            }

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
                }
            }
        }
    }

    private suspend fun loadGlossary(code: String?) {
        code?.let {
            val glossary = withContext(Dispatchers.IO) {
                glossaryRepository.getGlossary(it)
            }
            if (glossary != null) {
                _state.value = _state.value.copy(
                    activeGlossary = glossary.copy(getPhrases = {
                        runBlocking {
                            loadPhrases(glossary.id!!)
                        }
                    })
                )
            }
        }
    }

    private fun nextChapter() {
        screenModelScope.launch {
            _state.value.activeChapter?.let { chapter ->
                val current = chapter.number
                val next = current + 1
                val result = navigateChapter(next)
                if (result is NavigationResult.NoChange) {
                    nextBook()
                } else {
                    _event.send(ReadEvent.OnNavigation(result))
                }
            }
        }
    }

    private fun prevChapter() {
        screenModelScope.launch {
            _state.value.activeChapter?.let { chapter ->
                val current = chapter.number
                val prev = current - 1
                val result = navigateChapter(prev)
                if (result is NavigationResult.NoChange) {
                    prevBook()
                } else {
                    _event.send(ReadEvent.OnNavigation(result))
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
                            _event.send(ReadEvent.OnNavigation(result))
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
                            _event.send(ReadEvent.OnNavigation(result))
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
                        }.sortedWith(
                            compareBy {
                                it.refs.first { ref ->
                                    ref.resource == resource.slug
                                            && ref.book == book.slug
                                            && ref.chapter == chapter.number.toString()
                                }.verse.toIntOrNull() ?: Int.MAX_VALUE
                            }
                        )

                        _state.value = _state.value.copy(chapterPhrases = phrases)
                    }
                }
            }
        }
    }

    private suspend fun loadPhrases(glossaryId: String): List<Phrase> {
        return withContext(Dispatchers.IO) {
            glossaryRepository.getPhrases(glossaryId)
                .map {
                    it.copy(getRefs = {
                        runBlocking { loadRefs(it.id!!) }
                    })
                }
        }
    }

    private suspend fun loadRefs(phraseId: String): List<Ref> {
        return withContext(Dispatchers.IO) {
            glossaryRepository.getRefs(phraseId)
        }
    }

    private fun onSavePhrase(phrase: String) {
        screenModelScope.launch {
            (_state.value.activeGlossary ?: run {
                val code = randomCode()
                val glossary = Glossary(code, "user")
                val id = glossaryRepository.addGlossary(glossary)
                id?.let {
                    val withId = glossary.copy(id = id)
                    _event.send(ReadEvent.GlossaryChanged(withId))
                    delay(100)
                    withId
                }
            })?.let { glossary ->
                val phrase = glossaryRepository.getPhrase(phrase, glossary.id!!)
                    ?: Phrase(
                        phrase = phrase,
                        glossaryId = glossary.id
                    )
                _event.send(ReadEvent.SavePhrase(phrase))
            }
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(ReadEvent.Idle)
        }
    }
}