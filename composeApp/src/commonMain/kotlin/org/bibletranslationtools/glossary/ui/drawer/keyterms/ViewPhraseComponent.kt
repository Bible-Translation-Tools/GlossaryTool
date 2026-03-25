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
    fun onEditClick(phrase: Phrase)
}

class DefaultViewPhraseComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val phrase: Phrase,
    private val onNavigateRef: (Phrase, Ref) -> Unit,
    private val onNavigateEdit: (Phrase) -> Unit
) : DrawerComponent(componentContext, parentContext), ViewPhraseComponent, KoinComponent {

    private val appStateStore: AppStateStore by inject()
    private val resourceState = appStateStore.resourceStateHolder.state

    private val _model = MutableValue(ViewPhraseComponent.Model())
    override val model: Value<ViewPhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            componentScope.launch {
                _model.update { it.copy(isLoading = true) }

                val refs = withContext(Dispatchers.Default) {
                    findRefs(phrase)
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
        onNavigateRef(phrase, ref)
    }

    override fun onEditClick(phrase: Phrase) {
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