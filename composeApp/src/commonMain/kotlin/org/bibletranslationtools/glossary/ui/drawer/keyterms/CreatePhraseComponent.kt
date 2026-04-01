package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.asFlow
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.normalize
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.text.Regex.Companion.escape

private const val MAX_SEARCH_RESULTS = 100

interface CreatePhraseComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        val results: List<Phrase> = emptyList()
    )

    fun onSearchQueryChanged(query: String)
    fun onEditClick(phrase: Phrase)
}

@OptIn(FlowPreview::class)
class DefaultCreatePhraseComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onNavigateEdit: (phrase: Phrase) -> Unit
) : DrawerComponent(componentContext, parentContext), CreatePhraseComponent, KoinComponent {
    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(CreatePhraseComponent.Model())
    override val model: Value<CreatePhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val resourceState = appStateStore.resourceStateHolder.state
    private val glossaryState = appStateStore.glossaryStateHolder.state
    private val sourceVerses = mutableListOf<String>()
    private var existentPhrases: Set<Phrase> = emptySet()

    init {
        componentScope.launch {
            val verses = withContext(Dispatchers.Default) {
                resourceState.value.resource?.let { resource ->
                    resource.books.flatMap { it.chapters }
                        .flatMap { it.verses }
                        .map { it.text }
                } ?: emptyList()
            }

            sourceVerses.addAll(verses)

            glossaryState.value.glossary?.let { glossary ->
                val saved = glossaryRepository.getPhrases(glossary.id)
                val pending = glossaryRepository.getPendingPhrases(glossary.id)
                existentPhrases = (saved + pending).associateBy { it.phrase }
                    .values
                    .toSet()
            }
        }

        componentScope.launch {
            model.asFlow().map { it.searchQuery }
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _model.update { it.copy(searchQuery = query) }
    }

    override fun onEditClick(phrase: Phrase) {
        onNavigateEdit(phrase)
    }

    private suspend fun performSearch(query: String) {
        _model.update { it.copy(isSearching = true) }

        val results = withContext(Dispatchers.Default) {
            findWords(query)
        }

        _model.update {
            it.copy(
                results = results,
                isSearching = false
            )
        }
    }

    private fun findWords(query: String): List<Phrase> {
        val glossaryId = glossaryState.value.glossary?.id ?: return emptyList()
        if (query.isEmpty()) return emptyList()

        return sourceVerses.let { sourceSentences ->
            val lowerCaseQuery = query.normalize().lowercase()
            val queryWordCount = lowerCaseQuery.split(Regex("\\s+")).size

            val pattern = if (queryWordCount == 1) {
                "(?u)\\b${escape(lowerCaseQuery)}\\w*"
            } else {
                "(?u)\\b${escape(lowerCaseQuery)}[\\w\\s-]*"
            }
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)

            sourceSentences.asSequence()
                .map { it.normalize() }
                .filter { sentence ->
                    sentence.contains(lowerCaseQuery, ignoreCase = true)
                }
                .flatMap { sentence ->
                    regex.findAll(sentence).map { it.value.trim() }
                }
                .filter { foundPhrase ->
                    foundPhrase.split(Regex("(?u)\\s+")).size == queryWordCount
                }
                .distinct()
                .take(MAX_SEARCH_RESULTS)
                .map { word ->
                    val existent = existentPhrases.find { it.phrase.normalize() == word }
                    existent ?: Phrase(phrase = word, glossaryId = glossaryId)
                }
                .toList()
        }
    }
}