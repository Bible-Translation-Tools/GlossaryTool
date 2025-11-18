package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Glossary

data class GlossaryState(
    val glossary: Glossary? = null
)

interface GlossaryStateHolder {
    val state: StateFlow<GlossaryState>
    fun setGlossary(glossary: Glossary)
}

class GlossaryStateHolderImpl : GlossaryStateHolder {
    private val _state = MutableStateFlow(GlossaryState())
    override val state: StateFlow<GlossaryState> = _state

    override fun setGlossary(glossary: Glossary) {
        _state.update { current ->
            current.copy(glossary = glossary)
        }
    }
}
