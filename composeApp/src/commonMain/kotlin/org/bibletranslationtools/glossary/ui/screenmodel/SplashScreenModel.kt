package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.loading_glossary
import glossary.composeapp.generated.resources.loading_resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.getString

data class SplashState(
    val initDone: Boolean = false,
    val message: String? = null
)

class SplashScreenModel(
    private val initApp: InitApp,
    private val appStateStore: AppStateStore,
    private val workbookDataSource: WorkbookDataSource,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SplashState()
        )

    fun initializeApp(resourceSlug: String, glossaryCode: String?) {
        screenModelScope.launch {
            withContext(Dispatchers.IO) {
                initApp { message ->
                    _state.value = _state.value.copy(
                        message = message
                    )
                }
                loadResource(resourceSlug)
                loadGlossary(glossaryCode)
            }

            _state.value = _state.value.copy(
                initDone = true,
                message = null
            )
        }
    }

    private suspend fun loadResource(resourceSlug: String) {
        _state.value = _state.value.copy(
            message = getString(Res.string.loading_resources)
        )

        val books = withContext(Dispatchers.Default) {
            workbookDataSource.read(resourceSlug)
        }
        appStateStore.resourceStateHolder.updateResource(
            Resource(resourceSlug, books)
        )
    }

    private suspend fun loadGlossary(glossaryCode: String?) {
        _state.value = _state.value.copy(
            message = getString(Res.string.loading_glossary)
        )

        withContext(Dispatchers.Default) {
            glossaryCode?.let { code ->
                glossaryRepository.getGlossary(code)?.let { glossary ->
                    appStateStore.glossaryStateHolder.updateGlossary(
                        glossary.copy {
                            runBlocking {
                                loadPhrases(glossary.id!!)
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun loadPhrases(glossaryId: String): List<Phrase> {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getPhrases(glossaryId)
                .map {
                    it.copy(getRefs = {
                        runBlocking { loadRefs(it.id!!) }
                    })
                }
        }
    }

    private suspend fun loadRefs(phraseId: String): List<Ref> {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getRefs(phraseId)
        }
    }
}