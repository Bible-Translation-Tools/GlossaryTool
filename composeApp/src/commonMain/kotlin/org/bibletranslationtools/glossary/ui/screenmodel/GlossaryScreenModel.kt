package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class GlossaryState(
    val isLoading: Boolean = false
)

class GlossaryScreenModel : ScreenModel {
    private var _state = MutableStateFlow(GlossaryState())
    val state: StateFlow<GlossaryState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GlossaryState()
        )
}