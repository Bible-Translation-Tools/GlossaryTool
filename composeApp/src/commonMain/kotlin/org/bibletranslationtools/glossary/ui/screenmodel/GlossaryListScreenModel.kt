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
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.state.AppStateStore

data class GlossaryItem(
    val glossary: Glossary,
    val phraseCount: Int,
    val userCount: Int
)

data class GlossariesState(
    val isLoading: Boolean = false,
    val selectedGlossary: GlossaryItem? = null,
    val glossaries: List<GlossaryItem> = emptyList()
)

class GlossaryListScreenModel(
    appStateStore: AppStateStore,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(GlossariesState())
    val state: StateFlow<GlossariesState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GlossariesState()
        )

    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState

    init {
        loadGlossaries()
    }

    fun selectGlossary(glossary: GlossaryItem) {
        _state.update {
            it.copy(selectedGlossary = glossary)
        }
    }

    private fun loadGlossaries() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val glossaries = withContext(Dispatchers.Default) {
                glossaryRepository.getGlossaries()
            }

            val glossaryItems = withContext(Dispatchers.Default) {
                glossaries.map { glossary ->
                    val phraseCount = glossaryRepository.getPhrases(glossary.id).size
                    val userCount = 8 // TODO implement
                    GlossaryItem(
                        glossary = glossary,
                        phraseCount = phraseCount,
                        userCount = userCount
                    )
                }
            }

            val selectedGlossary = glossaryItems.singleOrNull {
                it == glossaryState.value.glossary
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    selectedGlossary = selectedGlossary,
                    glossaries = glossaryItems
                )
            }
        }
    }
}