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
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository

data class GlossaryState(
    val isLoading: Boolean = false,
    val activeGlossary: Glossary? = null
)

sealed class GlossaryEvent {
    data object Idle : GlossaryEvent()
    data class LoadGlossary(val code: String) : GlossaryEvent()
}

class GlossaryScreenModel(
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {
    private var _state = MutableStateFlow(GlossaryState())
    val state: StateFlow<GlossaryState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GlossaryState()
        )

    private val _event: Channel<GlossaryEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: GlossaryEvent) {
        when (event) {
            is GlossaryEvent.LoadGlossary -> loadGlossary(event.code)
            else -> resetChannel()
        }
    }

    private fun loadGlossary(code: String) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val glossary = withContext(Dispatchers.IO) {
                glossaryRepository.getGlossary(code)
            }

            _state.value = _state.value.copy(
                activeGlossary = glossary?.copy {
                    runBlocking {
                        loadPhrases(glossary.id!!)
                    }
                },
                isLoading = false
            )
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

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(GlossaryEvent.Idle)
        }
    }
}