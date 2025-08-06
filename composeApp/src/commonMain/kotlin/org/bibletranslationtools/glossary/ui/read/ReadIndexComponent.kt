package org.bibletranslationtools.glossary.ui.read

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class NavDir(val incr: Int) {
    NEXT(1),
    PREV(-1)
}

interface ReadIndexComponent : ParentContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val activeBook: Workbook? = null,
        val activeChapter: Chapter? = null,
        val chapterPhrases: List<Phrase> = emptyList(),
        val currentRef: RefOption? = null
    )

    fun nextChapter()
    fun prevChapter()
    fun navigateBookChapter(bookSlug: String, chapter: Int)
    fun onBrowseClick(book: String, chapter: Int)
    fun loadRef(ref: RefOption?)
    fun clearRef()
    fun onEditPhraseSelected(phrase: String)
    fun selectPhraseForView(phrase: Phrase)
    fun onPhraseClick(phrase: Phrase, verse: String?)
}

class DefaultReadIndexComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val ref: RefOption? = null,
    private val onNavigateViewPhrase: (phrase: Phrase) -> Unit,
    private val onNavigateEditPhrase: (phrase: String) -> Unit,
    private val onPhraseSelected: (
        phrase: Phrase,
        phrases: List<Phrase>,
        book: Workbook,
        chapter: Chapter,
        verse: String?
    ) -> Unit,
    private val onNavigateBrowse: (book: String, chapter: Int) -> Unit
) : AppComponent(componentContext, parentContext),
    ReadIndexComponent, KoinComponent {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(ReadIndexComponent.Model())
    override val model: Value<ReadIndexComponent.Model> = _model

    private val resourceState = appStateStore.resourceStateHolder.resourceState
    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        componentScope.launch {
            _model.update { it.copy(currentRef = ref) }
        }
    }

    override fun nextChapter() {
        componentScope.launch {
            navigateChapter(NavDir.NEXT)
        }
    }

    override fun prevChapter() {
        componentScope.launch {
            navigateChapter(NavDir.PREV)
        }
    }

    override fun navigateBookChapter(bookSlug: String, chapter: Int) {
        resourceState.value.resource?.books
            ?.find { it.slug == bookSlug }?.let { book ->
                book.chapters.find { it.number == chapter }?.let { chapter ->
                    navigateBookChapter(book, chapter)
                }
            }
    }

    override fun onBrowseClick(book: String, chapter: Int) {
        onNavigateBrowse(book, chapter)
    }

    override fun loadRef(ref: RefOption?) {
        _model.update { it.copy(currentRef = ref) }
    }

    override fun clearRef() {
        _model.update { it.copy(currentRef = null) }
    }

    override fun onEditPhraseSelected(phrase: String) {
        onNavigateEditPhrase(phrase)
    }

    override fun onPhraseClick(phrase: Phrase, verse: String?) {
        val book = _model.value.activeBook ?: return
        val chapter = _model.value.activeChapter ?: return
        val phrases = _model.value.chapterPhrases

        onPhraseSelected(phrase, phrases, book, chapter, verse)
    }

    override fun selectPhraseForView(phrase: Phrase) {
        onNavigateViewPhrase(phrase)
    }

    private fun navigateChapter(dir: NavDir) {
        val book = _model.value.activeBook ?: return
        val chapter = _model.value.activeChapter ?: return

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
        val book = _model.value.activeBook ?: return
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

    private fun navigateBookChapter(book: Workbook, chapter: Chapter) {
        _model.value = _model.value.copy(
            activeBook = book,
            activeChapter = chapter
        )
        loadChapterPhrases()
    }

    private fun loadChapterPhrases() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch
            val book = _model.value.activeBook ?: return@launch
            val chapter = _model.value.activeChapter ?: return@launch

            val phrases = withContext(Dispatchers.Default) {
                glossaryRepository.getPhrases(glossary.id)
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

            _model.update { it.copy(chapterPhrases = phrases) }
        }
    }
}