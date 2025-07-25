package org.bibletranslationtools.glossary.ui.screenmodel

import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import kotlin.random.Random
import kotlin.text.Regex.Companion.escape

private const val MAX_SEARCH_RESULTS = 100
private const val RANDOM_WORD_SAMPLE_SIZE = 100
private const val MAX_RANDOM_ATTEMPTS = 500

data class SearchPhraseState(
    val isSearching: Boolean = false,
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val results: List<String> = emptyList()
)

class SearchPhraseScreenModel(
    glossary: Glossary,
    appStateStore: AppStateStore
) : ScreenModel {

    private var _state = MutableStateFlow(SearchPhraseState())
    val state: StateFlow<SearchPhraseState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchPhraseState()
        )

    private val resourceState = appStateStore.resourceStateHolder.resourceState.value
    private var sourceText: String? = null
    private var exclusions: Set<String> = emptySet()
    private var searchJob: Job? = null

    init {
        screenModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            sourceText = withContext(Dispatchers.Default) {
                resourceState.resource?.let { resource ->
                    resource.books.flatMap { it.chapters }
                        .flatMap { it.verses }
                        .joinToString { it.text }
                }
            }
            exclusions = glossary.phrases.map { it.phrase }
                .map { it.lowercase() }.toSet()

            onSearchQueryChanged(TextFieldValue(""))
        }
    }

    fun onSearchQueryChanged(query: TextFieldValue) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = screenModelScope.launch {
            _state.update { it.copy(isSearching = true) }

            delay(300L)

            val results = withContext(Dispatchers.Default) {
                findContent(query.text)
            }

            _state.update {
                it.copy(
                    results = results,
                    isSearching = false
                )
            }
        }
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