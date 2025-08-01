package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.create_glossary_error
import glossary.composeapp.generated.resources.downloading
import glossary.composeapp.generated.resources.resource_not_found
import glossary.composeapp.generated.resources.unknown_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.CatalogApi
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.NetworkResult
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.jetbrains.compose.resources.getString

data class ResourceRequest(
    val lang: String,
    val type: String? = null
)

data class CreateGlossaryState(
    val isSaving: Boolean = false,
    val sourceLanguage: Language? = null,
    val targetLanguage: Language? = null,
    val resourceRequest: ResourceRequest? = null,
    val error: String? = null,
    val progress: Progress? = null
)

sealed class CreateGlossaryEvent {
    data object Idle : CreateGlossaryEvent()
    data class OnGlossaryCreated(val glossary: Glossary): CreateGlossaryEvent()
    data class OnResourceDownloaded(val resource: Resource): CreateGlossaryEvent()
}

class CreateGlossaryScreenModel(
    private val glossaryRepository: GlossaryRepository,
    private val directoryProvider: DirectoryProvider,
    private val resourceContainerAccessor: ResourceContainerAccessor,
    private val catalogApi: CatalogApi
) : ScreenModel {

    private var _state = MutableStateFlow(CreateGlossaryState())
    val state: StateFlow<CreateGlossaryState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CreateGlossaryState()
        )

    private val _event: Channel<CreateGlossaryEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun setSourceLanguage(language: Language) {
        _state.update { it.copy(sourceLanguage = language) }
    }

    fun setTargetLanguage(language: Language) {
        _state.update { it.copy(targetLanguage = language) }
    }

    fun createGlossary(code: String) {
        screenModelScope.launch {
            val sourceLanguage = _state.value.sourceLanguage ?: return@launch
            val targetLanguage = _state.value.targetLanguage ?: return@launch

            _state.update { it.copy(isSaving = true) }

            val resource = withContext(Dispatchers.Default) {
                findResource()
            }

            resource?.let { res ->
                val glossary = Glossary(
                    code = code,
                    author = "User",
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    resourceId = res.id
                )

                val id = withContext(Dispatchers.Default) {
                    glossaryRepository.addGlossary(glossary)
                }

                var error: String? = null

                id?.let {
                    _event.send(CreateGlossaryEvent.OnGlossaryCreated(
                        glossary.copy(id = it)
                    ))
                } ?: run {
                    error = getString(Res.string.create_glossary_error)
                }

                _state.update {
                    it.copy(
                        isSaving = false,
                        error = error
                    )
                }
            } ?: run {
                _state.update {
                    it.copy(
                        isSaving = false,
                        resourceRequest = ResourceRequest(
                            sourceLanguage.slug
                        )
                    )
                }
            }
        }
    }

    fun downloadResource(request: ResourceRequest) {
        screenModelScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.downloading)
            )

            _state.update {
                it.copy(progress = progress)
            }

            val resources = withContext(Dispatchers.Default) {
                glossaryRepository.getResources(request.lang)
            }

            val resource = resources.find { it.type != "udb" && it.type != "ulb" }
                ?: resources.firstOrNull { it.type == "ulb" }

            var error: String? = null

            if (resource != null) {
                val id = "${resource.lang}_${resource.type}"
                val filename = "$id.zip"
                val result = catalogApi.downloadResource(resource.url)

                if (result is NetworkResult.Success) {
                    val path = directoryProvider.saveSource(result.data, filename)
                    val res = resourceContainerAccessor.read(path)!!.copy(url = resource.url)

                    glossaryRepository.addResource(res)
                    _event.send(CreateGlossaryEvent.OnResourceDownloaded(res))
                } else {
                    error = (result as NetworkResult.Error).message
                        ?: getString(Res.string.unknown_error)
                }
            } else {
                error = getString(Res.string.resource_not_found)
            }

            _state.update {
                it.copy(
                    progress = null,
                    error = error
                )
            }
        }
    }

    fun clearResourceRequest() {
        _state.update { it.copy(resourceRequest = null) }
    }

    private suspend fun findResource(): Resource? {
        return _state.value.sourceLanguage?.let { language ->
            val dbRes = glossaryRepository.getResources(language.slug)
                .singleOrNull {
                    it.type != "udb" && it.filename.isNotEmpty()
                }
            dbRes?.let {
                resourceContainerAccessor.read(dbRes.filename)
                    ?.copy(id = dbRes.id, url = dbRes.url)
            }
        }
    }
}