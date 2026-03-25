package org.bibletranslationtools.glossary.ui.drawer.keyterms

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
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.text.Regex.Companion.escape

private const val MAX_SEARCH_RESULTS = 100
private const val RANDOM_WORD_SAMPLE_SIZE = 100
private const val MAX_RANDOM_ATTEMPTS = 500

interface CreatePhraseComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val isSearching: Boolean = false,
        val results: List<Phrase> = emptyList()
    )

    fun onSearchQueryChanged(query: String)
    fun onEditClick(phrase: Phrase)
}

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
    private var searchJob: Job? = null

    init {
        componentScope.launch {
            _model.update { it.copy(isSearching = true) }
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

            onSearchQueryChanged("")
        }
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

    override fun onEditClick(phrase: Phrase) {
        onNavigateEdit(phrase)
    }

    private fun findContent(query: String): List<Phrase> {
        return if (query.isEmpty()) {
            getRandomWords()
        } else {
            findWords(query)
        }
    }

    private fun findWords(query: String): List<Phrase> {
        val glossaryId = glossaryState.value.glossary?.id ?: return emptyList()

        return sourceVerses.let { sourceSentences ->
            val lowerCaseQuery = query.lowercase()
            val queryWordCount = lowerCaseQuery.split(Regex("\\s+")).size

            val pattern = if (queryWordCount == 1) {
                "(?u)\\b${escape(lowerCaseQuery)}\\w*"
            } else {
                "(?u)\\b${escape(lowerCaseQuery)}[\\w\\s-]*"
            }
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)

            sourceSentences.asSequence()
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
                    val existent = existentPhrases.find { it.phrase == word }
                    existent ?: Phrase(phrase = word, glossaryId = glossaryId)
                }
                .toList()

        }
    }

    private fun getRandomWords(): List<Phrase> {
        val glossaryId = glossaryState.value.glossary?.id ?: return emptyList()

        val randomWords = mutableSetOf<String>()
        if (sourceVerses.isEmpty()) return emptyList()

        val wordRegex = Regex("(?u)\\b\\w+\\b")
        var attempts = 0

        while (randomWords.size < RANDOM_WORD_SAMPLE_SIZE && attempts < MAX_RANDOM_ATTEMPTS) {
            attempts++

            val randomSentence = sourceVerses.random()
            val sentenceLength = randomSentence.length

            if (sentenceLength == 0) continue

            val randomStartIndex = Random.nextInt(sentenceLength)

            wordRegex.find(randomSentence, startIndex = randomStartIndex)?.let { matchResult ->
                val word = matchResult.value
                if (word.length > 2) {
                    randomWords.add(word)
                }
            }
        }

        return randomWords
            .map { word ->
                val existent = existentPhrases.find { it.phrase == word }
                existent ?: Phrase(phrase = word, glossaryId = glossaryId)
            }
            .toList()
    }
}