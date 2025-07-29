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
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository

data class ViewPhraseState(
    val isLoading: Boolean = false,
    val refs: List<Ref> = emptyList()
)

class ViewPhraseScreenModel(
    private val phrase: Phrase,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(ViewPhraseState())
    val state: StateFlow<ViewPhraseState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewPhraseState()
        )

    init {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val refs = withContext(Dispatchers.Default) {
                glossaryRepository.getRefs(phrase.id)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    refs = refs
                )
            }
        }
    }
}