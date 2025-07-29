package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Glossary

data class GlossaryState(
    val glossary: Glossary? = null
)

interface GlossaryStateHolder {
    val glossaryState: StateFlow<GlossaryState>
    fun updateGlossary(glossary: Glossary)
}

class GlossaryStateHolderImpl : GlossaryStateHolder {
    private val _glossaryState = MutableStateFlow(GlossaryState())
    override val glossaryState: StateFlow<GlossaryState> = _glossaryState

    override fun updateGlossary(glossary: Glossary) {
        _glossaryState.update { current ->
            current.copy(glossary = glossary)
        }
    }
}
