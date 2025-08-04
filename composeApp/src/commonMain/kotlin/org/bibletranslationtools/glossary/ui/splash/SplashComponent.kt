package org.bibletranslationtools.glossary.ui.splash

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.loading_glossary
import glossary.composeapp.generated.resources.loading_resources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SplashComponent {
    val model: Value<Model>

    fun initializeApp(resource: String, glossaryCode: String?)

    data class Model(
        val message: String? = null
    )
}

class DefaultSplashComponent(
    componentContext: ComponentContext,
    private val onInitDone: () -> Unit
) : SplashComponent, KoinComponent, ComponentContext by componentContext {

    private val initApp: InitApp by inject()
    private val appStateStore: AppStateStore by inject()
    private val workbookDataSource: WorkbookDataSource by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _model = MutableValue(SplashComponent.Model())
    override val model: Value<SplashComponent.Model> = _model

    override fun initializeApp(resource: String, glossaryCode: String?) {
        componentScope.launch {
            withContext(Dispatchers.IO) {
                initApp { message ->
                    _model.update { it.copy(message = message) }
                }
                loadResource(resource)
                loadGlossary(glossaryCode)
            }

            _model.update { it.copy(message = null) }
            onInitDone()
        }
    }

    private suspend fun loadResource(resourceId: String) {
        _model.value = _model.value.copy(
            message = getString(Res.string.loading_resources)
        )

        delay(2000)

        val resource = withContext(Dispatchers.Default) {
            val (lang, type) = resourceId.split("_")
            val dbRes = glossaryRepository.getResource(lang, type)
            val res = dbRes?.let { workbookDataSource.read(it.filename) }

            if (res == null) throw IllegalArgumentException("Resource not found in database")

            res.copy(id = dbRes.id, url = dbRes.url)
        }

        appStateStore.resourceStateHolder.updateResource(resource)
    }

    private suspend fun loadGlossary(glossaryCode: String?) {
        _model.value = _model.value.copy(
            message = getString(Res.string.loading_glossary)
        )

        withContext(Dispatchers.Default) {
            glossaryCode?.let { code ->
                glossaryRepository.getGlossary(code)?.let { glossary ->
                    appStateStore.glossaryStateHolder.updateGlossary(glossary)
                }
            }
        }
    }
}