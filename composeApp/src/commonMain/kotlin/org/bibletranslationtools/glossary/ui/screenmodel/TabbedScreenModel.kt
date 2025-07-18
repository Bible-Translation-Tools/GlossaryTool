package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook

data class PhraseDetails(
    val phrase: Phrase,
    val resource: Resource,
    val book: Workbook,
    val chapter: Chapter,
    val verse: String
)

data class TabbedState(
    val phraseDetails: PhraseDetails? = null
)

sealed class TabbedEvent {
    data object Idle : TabbedEvent()
    data class LoadPhrase(val phraseDetails: PhraseDetails?) : TabbedEvent()
}

class TabbedScreenModel : ScreenModel {

    private var _state = MutableStateFlow(TabbedState())
    val state: StateFlow<TabbedState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TabbedState()
        )

    private val _event: Channel<TabbedEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: TabbedEvent) {
        when (event) {
            is TabbedEvent.LoadPhrase -> loadPhrase(event.phraseDetails)
            else -> resetChannel()
        }
    }

    private fun loadPhrase(phraseDetails: PhraseDetails?) {
        _state.value = _state.value.copy(
            phraseDetails = phraseDetails
        )
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(TabbedEvent.Idle)
        }
    }
}