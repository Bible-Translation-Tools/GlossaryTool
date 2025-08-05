package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.main.ComposableSlot
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ViewPhraseComponent {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val phrase: Phrase? = null,
        val refs: List<Ref> = emptyList()
    )

    fun onBackClick()
    fun onRefClick(ref: RefOption)
    fun onEditClick(phrase: String)
    fun setTopBar(slot: ComposableSlot?)
}

class DefaultViewPhraseComponent(
    componentContext: ComponentContext,
    private val phrase: Phrase,
    private val onNavigateBack: () -> Unit,
    private val onNavigateRef: (RefOption) -> Unit,
    private val onNavigateEdit: (String) -> Unit,
    private val onSetTopBar: (ComposableSlot?) -> Unit
) : ViewPhraseComponent, KoinComponent, ComponentContext by componentContext {

    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(ViewPhraseComponent.Model())
    override val model: Value<ViewPhraseComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val refs = withContext(Dispatchers.Default) {
                phrase.id?.let { glossaryRepository.getRefs(it) }
            } ?: emptyList()

            _model.update {
                it.copy(
                    isLoading = false,
                    phrase = phrase,
                    refs = refs
                )
            }
        }
        lifecycle.doOnDestroy {
            setTopBar(null)
        }
    }

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun onRefClick(ref: RefOption) {
        onNavigateRef(ref)
    }

    override fun onEditClick(phrase: String) {
        onNavigateEdit(phrase)
    }

    override fun setTopBar(slot: ComposableSlot?) {
        onSetTopBar(slot)
    }
}