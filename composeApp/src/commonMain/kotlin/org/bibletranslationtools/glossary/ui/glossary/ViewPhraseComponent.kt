package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.AppComponent
import org.bibletranslationtools.glossary.ui.ParentContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ViewPhraseComponent : ParentContext {
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
    parentContext: ParentContext,
    private val phraseId: String,
    private val onNavigateBack: () -> Unit,
    private val onNavigateRef: (String, Ref) -> Unit,
    private val onNavigateEdit: (String) -> Unit
) : AppComponent(componentContext, parentContext),
    ViewPhraseComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(ViewPhraseComponent.Model())
    override val model: Value<ViewPhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            componentScope.launch {
                _model.update { it.copy(isLoading = true) }

                val phrase = glossaryRepository.getPhrase(phraseId)
                val refs = phrase?.id?.let { glossaryRepository.getRefs(it) }
                    ?.sortedWith(
                        compareBy<Ref> {
                            Utils.bookOrderMap()[it.book] ?: Int.MAX_VALUE
                        }
                            .thenBy { it.chapter }
                            .thenBy { it.verse }
                    )
                    ?: emptyList()

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

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun onRefClick(ref: Ref) {
        onNavigateRef(phraseId, ref)
    }

    override fun onEditClick(phrase: String) {
        onNavigateEdit(phrase)
    }
}