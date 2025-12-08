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
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.platform.showNavigationBar
import org.bibletranslationtools.glossary.ui.AppComponent
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.components.PhraseDetails
import org.bibletranslationtools.glossary.ui.components.PhraseNavDir
import org.bibletranslationtools.glossary.ui.main.MainStateKeeper
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
        val phraseDetails: PhraseDetails? = null,
        val currentRef: RefOption? = null,
        val keyTermsDrawerOpen: Boolean = false,
        val settingsDrawerOpen: Boolean = false
    )

    fun nextChapter()
    fun prevChapter()
    fun navigateBookChapter(bookSlug: String, chapter: Int)
    fun onBrowseClick(book: String, chapter: Int)
    fun loadRef(ref: RefOption?)
    fun reloadChapter()
    fun clearRef()
    fun onPhraseSelected(phrase: String)
    fun onPhraseClick(phrase: Phrase, verse: String?)
    fun navigatePhrase(dir: PhraseNavDir)
    fun onViewPhraseClick(phraseId: String)
    fun clearPhraseDetails()
}

class DefaultReadIndexComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val ref: RefOption? = null,
    private val sharedState: MainStateKeeper,
    private val onNavigateViewPhrase: (phraseId: String) -> Unit,
    private val onNavigateEditPhrase: (phrase: String) -> Unit,
    private val onNavigateBrowse: (book: String, chapter: Int) -> Unit
) : AppComponent(componentContext, parentContext),
    ReadIndexComponent, KoinComponent {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(ReadIndexComponent.Model())
    override val model: Value<ReadIndexComponent.Model> = _model

    private val resourceState = appStateStore.resourceStateHolder.state
    private val glossaryState = appStateStore.glossaryStateHolder.state

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        componentScope.launch {
            _model.update { it.copy(currentRef = ref) }

            sharedState.model.subscribe { model ->
                if (model.phraseUpdated) {
                    loadChapterPhrases()
                    sharedState.updatePhraseUpdated(false)
                }
                _model.update {
                    it.copy(
                        keyTermsDrawerOpen = model.keyTermsDrawerOpen,
                        settingsDrawerOpen = model.settingsDrawerOpen
                    )
                }
            }
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

    override fun reloadChapter() {
        model.value.activeBook?.let { workbook ->
            model.value.activeChapter?.let { chapter ->
                navigateBookChapter(workbook.slug, chapter.number)
            }
        }
    }

    override fun onPhraseSelected(phrase: String) {
        onNavigateEditPhrase(phrase)
    }

    override fun onPhraseClick(phrase: Phrase, verse: String?) {
        val book = _model.value.activeBook ?: return
        val chapter = _model.value.activeChapter ?: return
        val phrases = _model.value.chapterPhrases

        loadPhrase(phrase, phrases, book, chapter, verse)
    }

    override fun navigatePhrase(dir: PhraseNavDir) {
        componentScope.launch {
            navigatePhrase(dir.value)
        }
    }

    override fun onViewPhraseClick(phraseId: String) {
        onNavigateViewPhrase(phraseId)
    }

    override fun clearPhraseDetails() {
        showNavigationBar(true)
        _model.update { it.copy(phraseDetails = null) }
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
                val saved = glossaryRepository.getPhrases(glossary.id)
                val pending = glossaryRepository.getPendingPhrases(glossary.id)
                (saved + pending).associateBy { it.id }.values.toList()
                    .mapNotNull { phrase ->
                        val relevantRef = findRelevantRefs(phrase, book, chapter).firstOrNull()
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

    private fun findRelevantRefs(
        phrase: Phrase,
        book: Workbook,
        chapter: Chapter
    ): List<Ref> {
        val resource = resourceState.value.resource ?: return emptyList()

        val regex = Regex(
            pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
            option = RegexOption.IGNORE_CASE
        )
        val refs = mutableListOf<Ref>()
        val verses = resource.books.singleOrNull {
            book.slug == it.slug
        }
            ?.chapters?.singleOrNull {
                chapter.number == it.number
            }?.verses ?: return emptyList()

        for (verse in verses) {
            val matchCount = regex.findAll(verse.text).count()
            if (matchCount > 0) {
                repeat(matchCount) {
                    refs.add(
                        Ref(
                            book = book.slug,
                            chapter = chapter.number.toString(),
                            verse = verse.number,
                            phraseId = phrase.id
                        )
                    )
                }
            }
        }
        return refs
    }

    private fun loadPhrase(
        phrase: Phrase,
        phrases: List<Phrase>,
        book: Workbook,
        chapter: Chapter,
        verse: String?
    ) {
        showNavigationBar(false)
        componentScope.launch {
            val ref = getInitialRef(
                phrase = phrase,
                book = book,
                chapter = chapter,
                verse = verse
            )
            val phraseDetails = PhraseDetails(
                phrase = phrase,
                phrases = phrases,
                ref = ref,
                book = book,
                chapter = chapter,
                verse = verse
            )

            _model.value = _model.value.copy(
                phraseDetails = phraseDetails
            )
        }
    }

    private suspend fun navigatePhrase(incr: Int) {
        _model.value.phraseDetails?.let { details ->
            details.phrases.getOrNull(
                details.phrases.indexOf(details.phrase) + incr
            )?.let { phrase ->
                val ref = getInitialRef(
                    phrase = phrase,
                    book = details.book,
                    chapter = details.chapter
                )
                _model.update { state ->
                    state.copy(
                        phraseDetails = details.copy(
                            phrase = phrase,
                            ref = ref
                        )
                    )
                }
            }
        }
    }

    private suspend fun getInitialRef(
        phrase: Phrase,
        book: Workbook,
        chapter: Chapter,
        verse: String? = null,
    ): Ref? {
        return withContext(Dispatchers.Default) {
            findRelevantRefs(phrase, book, chapter).firstOrNull {
                verse == null || it.verse == verse
            }
        }
    }
}