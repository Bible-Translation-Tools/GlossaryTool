package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.no_refs_found
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.main.ComposableSlot
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface EditPhraseComponent {

    val model: Value<Model>

    data class Model(
        val isSaving: Boolean = false,
        val phrase: Phrase? = null,
        val error: String? = null
    )

    fun savePhrase(spelling: String, description: String)
    fun onBackClick()
    fun setTopBar(slot: ComposableSlot?)
}

class DefaultEditPhraseComponent(
    componentContext: ComponentContext,
    private val phrase: String,
    private val onPhraseSaved: () -> Unit,
    private val onNavigateBack: () -> Unit,
    private val onSetTopBar: (ComposableSlot?) -> Unit
) : EditPhraseComponent, KoinComponent, ComponentContext by componentContext {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(EditPhraseComponent.Model())
    override val model: Value<EditPhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val resourceState = appStateStore.resourceStateHolder.resourceState
    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState

    init {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch

            val phrase = glossaryRepository.getPhrase(phrase, glossary.id!!)
                ?: Phrase(
                    phrase = phrase,
                    glossaryId = glossary.id
                )

            _model.update { it.copy(phrase = phrase) }
        }
        lifecycle.doOnDestroy {
            setTopBar(null)
        }
    }

    override fun savePhrase(spelling: String, description: String) {
        componentScope.launch {
            _model.value = _model.value.copy(
                isSaving = true,
                error = null
            )

            val error = withContext(Dispatchers.IO) {
                _model.value.phrase?.let { phrase ->
                    val phrase = phrase.copy(
                        spelling = spelling,
                        description = description,
                        updatedAt = getCurrentTime(),
                        id = phrase.id
                    )
                    val dbRefs = phrase.id?.let {
                        glossaryRepository.getRefs(it)
                    } ?: emptyList()

                    val refs = (dbRefs + findRefs(phrase)).distinctBy {
                        listOf(it.book, it.chapter, it.verse)
                    }

                    if (refs.isNotEmpty()) {
                        glossaryRepository.addPhrase(phrase)?.let { phraseId ->
                            refs.forEach { ref ->
                                glossaryRepository.addRef(ref.copy(phraseId = phraseId))
                            }
                        }
                        null
                    } else {
                        getString(Res.string.no_refs_found)
                    }
                }
            }

            _model.update {
                it.copy(
                    isSaving = false,
                    error = error
                )
            }

            if (error == null) {
                onPhraseSaved()
            }
        }
    }

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun setTopBar(slot: ComposableSlot?) {
        onSetTopBar(slot)
    }

    private fun findRefs(phrase: Phrase): List<Ref> {
        val refs = mutableListOf<Ref>()
        resourceState.value.resource?.let { resource ->
            resource.books.forEach { book ->
                book.chapters.forEach { chapter ->
                    chapter.verses.forEach { verse ->
                        val regex = Regex(
                            pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
                            option = RegexOption.IGNORE_CASE
                        )
                        val count = regex.findAll(verse.text).count()
                        repeat(count) {
                            val ref = Ref(
                                book = book.slug,
                                chapter = chapter.number.toString(),
                                verse = verse.number,
                                phraseId = phrase.id
                            )
                            refs.add(ref)
                        }
                    }
                }
            }
        }
        return refs
    }
}