package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository

data class GlossaryState(
    val glossary: Glossary? = null
)

interface GlossaryStateHolder {
    val glossaryState: StateFlow<GlossaryState>
    fun updateGlossary(glossary: Glossary)
}

class GlossaryStateHolderImpl(
    private val glossaryRepository: GlossaryRepository
) : GlossaryStateHolder {
    private val _glossaryState = MutableStateFlow(GlossaryState())
    override val glossaryState: StateFlow<GlossaryState> = _glossaryState

    override fun updateGlossary(glossary: Glossary) {
        _glossaryState.update { current ->
            current.copy(
                glossary = glossary.let { g ->
                    g.copy(getPhrases = {
                        runBlocking { loadPhrases(g.id!!) }
                    })
                }
            )
        }
    }

    private suspend fun loadPhrases(glossaryId: String): List<Phrase> {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getPhrases(glossaryId)
                .map {
                    it.copy(getRefs = {
                        runBlocking {
                            loadRefs(it.id!!)
                        }
                    })
                }
        }
    }

    private suspend fun loadRefs(phraseId: String): List<Ref> {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getRefs(phraseId)
        }
    }
}
