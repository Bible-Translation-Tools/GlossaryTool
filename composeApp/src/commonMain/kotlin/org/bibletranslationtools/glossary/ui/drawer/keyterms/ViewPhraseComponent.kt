package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ViewPhraseComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val phrase: Phrase? = null,
        val refs: List<Ref> = emptyList()
    )

    fun onRefClick(ref: Ref)
    fun onEditClick(phrase: String)
}

class DefaultViewPhraseComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val phraseId: String,
    private val onNavigateRef: (String, Ref) -> Unit,
    private val onNavigateEdit: (String) -> Unit
) : DrawerComponent(componentContext, parentContext), ViewPhraseComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()

    private val appStateStore: AppStateStore by inject()
    private val resourceState = appStateStore.resourceStateHolder.state

    private val _model = MutableValue(ViewPhraseComponent.Model())
    override val model: Value<ViewPhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            componentScope.launch {
                _model.update { it.copy(isLoading = true) }

                val (phrase, refs) = withContext(Dispatchers.Default) {
                    val p = glossaryRepository.getPendingPhrase(phraseId)
                        ?: glossaryRepository.getPhrase(phraseId)
                    val r = p?.let { findRefs(it) } ?: emptyList()
                    p to r
                }

                _model.update {
                    it.copy(
                        isLoading = false,
                        phrase = phrase,
                        refs = refs
                    )
                }
            }
        }
    }

    override fun onRefClick(ref: Ref) {
        onNavigateRef(phraseId, ref)
    }

    override fun onEditClick(phrase: String) {
        onNavigateEdit(phrase)
    }

    private fun findRefs(phrase: Phrase): List<Ref> {
        val resource = resourceState.value.resource ?: return emptyList()

        val regex = Regex(
            pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
            option = RegexOption.IGNORE_CASE
        )
        val refs = mutableListOf<Ref>()

        for (book in resource.books) {
            for (chapter in book.chapters) {
                for (verse in chapter.verses) {
                    val matchCount = regex.findAll(verse.text).count()
                    if (matchCount > 0) {
                        repeat(matchCount) {
                            refs.add(
                                Ref(
                                    book = book.slug,
                                    chapter = chapter.number.toString(),
                                    verse = verse.number,
                                    phraseId = phrase.id
                                )
                            )
                        }
                    }
                }
            }
        }
        return refs
    }
}