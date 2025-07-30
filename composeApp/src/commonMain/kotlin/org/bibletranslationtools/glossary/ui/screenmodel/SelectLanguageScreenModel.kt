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
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.domain.GlossaryRepository

data class SearchLanguageState(
    val isLoading: Boolean = false,
    val languages: List<Language> = emptyList()
)

class SelectLanguageScreenModel(
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(SearchLanguageState())
    val state: StateFlow<SearchLanguageState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchLanguageState()
        )

    init {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val languages = withContext(Dispatchers.Default) {
                glossaryRepository.getAllLanguages()
            }
            _state.value = _state.value.copy(
                isLoading = false,
                languages = languages
            )
        }
    }
}