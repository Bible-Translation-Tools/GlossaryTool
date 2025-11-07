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
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Verse
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.main.DrawerContext
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ViewChapterComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val phrase: Phrase? = null,
        val ref: Ref? = null,
        val verses: List<Verse> = emptyList()
    )
}

class DefaultViewChapterComponent(
    componentContext: ComponentContext,
    private val parentContext: DrawerContext,
    private val phraseId: String,
    private val ref: Ref,
    private val setFullscreen: (Boolean) -> Unit
) : ViewChapterComponent, KoinComponent, ComponentContext by componentContext {

    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(ViewChapterComponent.Model())
    override val model: Value<ViewChapterComponent.Model> = _model

    private val appStateStore: AppStateStore by inject()
    private val resourceState = appStateStore.resourceStateHolder.resourceState

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        setFullscreen(true)

        doOnResume {
            componentScope.launch {
                _model.update { it.copy(isLoading = true) }

                val phrase = glossaryRepository.getPhrase(phraseId)
                val verses = resourceState.value.resource?.let {
                    ref.getChapterVerses(it)
                } ?: emptyList()

                _model.update {
                    it.copy(
                        isLoading = false,
                        phrase = phrase,
                        ref = ref,
                        verses = verses
                    )
                }
            }
        }
    }

    override fun dismiss() {
        navigateBack()
    }

    override fun navigateBack() {
        parentContext.navigateBack()
    }
}