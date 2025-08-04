package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.text.Regex.Companion.escape

private const val MAX_SEARCH_RESULTS = 100
private const val RANDOM_WORD_SAMPLE_SIZE = 100
private const val MAX_RANDOM_ATTEMPTS = 500

interface SearchPhrasesComponent {
    val model: Value<Model>

    data class Model(
        val isSearching: Boolean = false,
        val results: List<String> = emptyList()
    )

    fun onBackClicked()
    fun onSearchQueryChanged(query: String)
    fun onEditClick(phrase: String)
}

class DefaultSearchPhrasesComponent(
    componentContext: ComponentContext,
    private val onNavigateBack: () -> Unit,
    private val onNavigateEdit: (phrase: String) -> Unit
) : SearchPhrasesComponent, KoinComponent, ComponentContext by componentContext {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(SearchPhrasesComponent.Model())
    override val model: Value<SearchPhrasesComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val resourceState = appStateStore.resourceStateHolder.resourceState
    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState
    private var sourceText: String? = null
    private var exclusions: Set<String> = emptySet()
    private var searchJob: Job? = null

    init {
        componentScope.launch {
            _model.update { it.copy(isSearching = true) }
            sourceText = withContext(Dispatchers.Default) {
                resourceState.value.resource?.let { resource ->
                    resource.books.flatMap { it.chapters }
                        .flatMap { it.verses }
                        .joinToString { it.text }
                }
            }
            glossaryState.value.glossary?.let { glossary ->
                exclusions = glossaryRepository.getPhrases(glossary.id)
                    .map { it.phrase }
                    .map { it.lowercase() }
                    .toSet()
            }

            onSearchQueryChanged("")
        }
    }

    override fun onBackClicked() {
        onNavigateBack()
    }

    override fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = componentScope.launch {
            _model.update { it.copy(isSearching = true) }

            delay(300L)

            val results = withContext(Dispatchers.Default) {
                findContent(query)
            }

            _model.update {
                it.copy(
                    results = results,
                    isSearching = false
                )
            }
        }
    }

    override fun onEditClick(phrase: String) {
        onNavigateEdit(phrase)
    }

    private fun findContent(query: String): List<String> {
        return if (query.isEmpty()) {
            getRandomWords()
        } else {
            findWords(query)
        }
    }

    private fun findWords(query: String): List<String> {
        return sourceText?.let { source ->
            val lowerCaseQuery = query.lowercase()
            val queryWordCount = lowerCaseQuery.split(Regex("\\s+")).size
            val pattern = if (queryWordCount == 1) {
                "(?u)\\b${escape(lowerCaseQuery)}\\w*"
            } else {
                "(?u)\\b${escape(lowerCaseQuery)}[\\w\\s-]*"
            }
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val matches = regex.findAll(source)
                .map { it.value.trim() }
                .filter { foundPhrase ->
                    foundPhrase.split(Regex("(?u)\\s+")).size == queryWordCount
                }
                .take(MAX_SEARCH_RESULTS)
                .toSet()
            matches.filterNot { it.lowercase() in exclusions }
        } ?: emptyList()
    }

    private fun getRandomWords(): List<String> {
        return sourceText?.let { source ->
            val randomWords = mutableSetOf<String>()
            val textLength = source.length
            if (textLength == 0) return emptyList()

            val wordRegex = Regex("(?u)\\b\\w+\\b")
            var attempts = 0

            while (randomWords.size < RANDOM_WORD_SAMPLE_SIZE && attempts < MAX_RANDOM_ATTEMPTS) {
                attempts++
                val randomStartIndex = Random.nextInt(textLength)

                wordRegex.find(source, startIndex = randomStartIndex)?.let { matchResult ->
                    val word = matchResult.value
                    if (word.length > 2 && word.lowercase() !in exclusions) {
                        randomWords.add(word)
                    }
                }
            }
            randomWords.toList()
        } ?: emptyList()
    }
}