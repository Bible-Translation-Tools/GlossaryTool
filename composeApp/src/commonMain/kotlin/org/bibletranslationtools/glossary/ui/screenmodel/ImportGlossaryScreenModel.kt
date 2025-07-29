package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class ImportGlossariesState(
    val isLoading: Boolean = false
)

class ImportGlossaryScreenModel : ScreenModel {

    private var _state = MutableStateFlow(ImportGlossariesState())
    val state: StateFlow<ImportGlossariesState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ImportGlossariesState()
        )
}