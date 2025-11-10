package org.bibletranslationtools.glossary.ui.drawer.settings

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
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class LanguageType {
    SOURCE, TARGET
}

interface SelectLanguageComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val type: LanguageType = LanguageType.SOURCE,
        val isLoading: Boolean = false,
        val languages: List<Language> = emptyList()
    )

    fun onLanguageClick(language: Language)
}

class DefaultSelectLanguageComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val type: LanguageType,
    private val sharedState: CreateGlossaryStateKeeper
) : DrawerComponent(componentContext, parentContext), SelectLanguageComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _model = MutableValue(SelectLanguageComponent.Model())
    override val model: Value<SelectLanguageComponent.Model> = _model

    init {
        doOnResume {
            setFullscreen(true)
        }

        componentScope.launch {
            _model.update {
                it.copy(
                    type = type,
                    isLoading = true
                )
            }
            val languages = withContext(Dispatchers.Default) {
                glossaryRepository.getAllLanguages()
            }
            _model.value = _model.value.copy(
                isLoading = false,
                languages = languages
            )
        }
    }

    override fun onLanguageClick(language: Language) {
        sharedState.onLanguageSelected(type, language)
        navigateBack()
    }
}