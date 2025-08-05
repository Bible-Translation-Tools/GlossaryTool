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
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.domain.GlossaryRepository

data class GlossaryState(
    val isLoading: Boolean = false,
    val phrases: List<Phrase> = emptyList()
)

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

    fun loadPhrases(glossary: Glossary) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val phrases = withContext(Dispatchers.Default) {
                glossaryRepository.getPhrases(glossary.id)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    phrases = phrases
                )
            }
        }
    }
}