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
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
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
    parentContext: DrawerContext,
    private val phrase: Phrase,
    private val ref: Ref
) : DrawerComponent(componentContext, parentContext), ViewChapterComponent, KoinComponent {

    private val _model = MutableValue(ViewChapterComponent.Model())
    override val model: Value<ViewChapterComponent.Model> = _model

    private val appStateStore: AppStateStore by inject()
    private val resourceState = appStateStore.resourceStateHolder.state

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            setFullscreen(true)

            componentScope.launch {
                _model.update { it.copy(isLoading = true) }

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
}