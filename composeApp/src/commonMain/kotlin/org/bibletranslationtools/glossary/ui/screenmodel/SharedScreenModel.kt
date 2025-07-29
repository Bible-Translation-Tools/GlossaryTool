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
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.components.PhraseNavDir

data class PhraseDetails(
    val phrase: Phrase,
    val phrases: List<Phrase>,
    val ref: Ref?,
    val resource: Resource,
    val book: Workbook,
    val chapter: Chapter,
    val verse: String? = null
)

data class TabbedState(
    val phraseDetails: PhraseDetails? = null,
    val currentRef: RefOption? = null
)

class SharedScreenModel(
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {
    private var _state = MutableStateFlow(TabbedState())
    val state: StateFlow<TabbedState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TabbedState()
        )

    fun loadPhrase(
        phrase: Phrase,
        phrases: List<Phrase>,
        resource: Resource,
        book: Workbook,
        chapter: Chapter,
        verse: String? = null
    ) {
        screenModelScope.launch {
            val ref = navigateRef(
                phrase = phrase,
                resource = resource,
                book = book,
                chapter = chapter,
                verse = verse
            )
            val phraseDetails = PhraseDetails(
                phrase = phrase,
                phrases = phrases,
                ref = ref,
                resource = resource,
                book = book,
                chapter = chapter,
                verse = verse
            )

            _state.value = _state.value.copy(
                phraseDetails = phraseDetails
            )
        }
    }

    fun loadRef(ref: RefOption?) {
        _state.update { it.copy(currentRef = ref) }
    }

    fun navigatePhrase(dir: PhraseNavDir) {
        screenModelScope.launch {
            navigatePhrase(dir.value)
        }
    }

    fun navigateRef(dir: PhraseNavDir) {
        screenModelScope.launch {
            navigateRef(dir.value)
        }
    }

    fun clearPhraseDetails() {
        _state.update { it.copy(phraseDetails = null) }
    }

    fun clearRef() {
        _state.update { it.copy(currentRef = null) }
    }

    private suspend fun navigatePhrase(incr: Int) {
        _state.value.phraseDetails?.let { details ->
            details.phrases.getOrNull(
                details.phrases.indexOf(details.phrase) + incr
            )?.let { phrase ->
                val ref = navigateRef(
                    phrase = phrase,
                    resource = details.resource,
                    book = details.book,
                    chapter = details.chapter
                )
                _state.update { state ->
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

    private suspend fun navigateRef(
        phrase: Phrase,
        resource: Resource,
        book: Workbook,
        chapter: Chapter,
        verse: String? = null,
    ): Ref? {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getRefs(phrase.id).firstOrNull {
                it.resource == resource.slug
                        && it.book == book.slug
                        && it.chapter == chapter.number.toString()
                        && (verse == null || it.verse == verse)
            }
        }
    }

    private suspend fun navigateRef(incr: Int) {
        withContext(Dispatchers.Default) {
            _state.value.phraseDetails?.let { details ->
                val refs = glossaryRepository.getRefs(details.phrase.id)
                refs.getOrNull(refs.indexOf(details.ref) + incr)
                    ?.let { ref ->
                        _state.update { state ->
                            state.copy(
                                phraseDetails = details.copy(
                                    ref = ref
                                )
                            )
                        }
                    }
            }
        }
    }
}