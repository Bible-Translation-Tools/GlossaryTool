package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook

data class PhraseDetails(
    val phrase: Phrase,
    val phrases: List<Phrase>,
    val resource: Resource,
    val book: Workbook,
    val chapter: Chapter,
    val verse: String = ""
)

data class TabbedState(
    val phraseDetails: PhraseDetails? = null,
    val currentRef: RefOption? = null
)

class TabbedScreenModel : ScreenModel {
    private var _state = MutableStateFlow(TabbedState())
    val state: StateFlow<TabbedState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TabbedState()
        )

    fun loadPhrase(phraseDetails: PhraseDetails?) {
        _state.value = _state.value.copy(
            phraseDetails = phraseDetails
        )
    }

    fun loadRef(ref: RefOption?) {
        _state.update { it.copy(currentRef = ref) }
    }

    fun clearRef() {
        _state.update { it.copy(currentRef = null) }
    }
}