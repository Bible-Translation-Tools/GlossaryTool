package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.no_refs_found
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.getString

sealed class EditPhraseEvent {
    data object Idle : EditPhraseEvent()
    data class SavePhrase(val spelling: String, val description: String) : EditPhraseEvent()
    data object OnPhraseSaved: EditPhraseEvent()
}

data class EditPhraseState(
    val isSaving: Boolean = false,
    val error: String? = null
)

class EditPhraseScreenModel(
    private val phrase: Phrase,
    private val appStateStore: AppStateStore,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(EditPhraseState())
    val state: StateFlow<EditPhraseState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditPhraseState()
        )

    private val _event: Channel<EditPhraseEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: EditPhraseEvent) {
        when (event) {
            is EditPhraseEvent.SavePhrase -> savePhrase(
                event.spelling,
                event.description
            )
            else -> resetChannel()
        }
    }

    private fun savePhrase(spelling: String, description: String) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            _state.value = _state.value.copy(error = null)

            withContext(Dispatchers.IO) {
                val phrase = phrase.copy(
                    spelling = spelling,
                    description = description,
                    updatedAt = getCurrentTime(),
                    id = phrase.id
                )
                val dbRefs = phrase.id?.let {
                    glossaryRepository.getRefs(it)
                } ?: emptyList()

                val refs = (dbRefs + findRefs(phrase)).distinctBy {
                    listOf(it.resource, it.book, it.chapter, it.verse)
                }

                if (refs.isNotEmpty()) {
                    glossaryRepository.addPhrase(phrase)?.let { phraseId ->
                        glossaryRepository.addRefs(
                            refs.map { ref -> ref.copy(phraseId = phraseId) }
                        )
                    }
                    _event.send(EditPhraseEvent.OnPhraseSaved)
                } else {
                    _state.value = _state.value.copy(
                        error = getString(Res.string.no_refs_found)
                    )
                }
            }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun findRefs(phrase: Phrase): List<Ref> {
        val refs = mutableListOf<Ref>()
        val resource = appStateStore.resourceStateHolder.resourceState.value.resource
        resource?.let { resource ->
            resource.books.forEach { book ->
                book.chapters.forEach { chapter ->
                    chapter.verses.forEach { verse ->
                        val regex = Regex(
                            pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
                            option = RegexOption.IGNORE_CASE
                        )
                        val count = regex.findAll(verse.text).count()
                        if (count > 0) {
                            for (i in 1..count) {
                                val ref = Ref(
                                    resource = resource.slug,
                                    book = book.slug,
                                    chapter = chapter.number.toString(),
                                    verse = verse.number,
                                    phraseId = phrase.id
                                )
                                refs.add(ref)
                            }
                        }
                    }
                }
            }
        }
        return refs
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(EditPhraseEvent.Idle)
        }
    }
}