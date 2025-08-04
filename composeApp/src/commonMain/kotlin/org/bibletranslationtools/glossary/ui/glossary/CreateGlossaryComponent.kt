package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.create_glossary_error
import glossary.composeapp.generated.resources.downloading
import glossary.composeapp.generated.resources.resource_not_found
import glossary.composeapp.generated.resources.unknown_error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface CreateGlossaryComponent {
    val model: Value<Model>

    data class ResourceRequest(
        val lang: String,
        val type: String? = null
    )

    data class Model(
        val isSaving: Boolean = false,
        val sourceLanguage: Language? = null,
        val targetLanguage: Language? = null,
        val resourceRequest: ResourceRequest? = null,
        val error: String? = null,
        val progress: Progress? = null
    )

    fun onBackClicked()
    fun createGlossary(code: String)
    fun downloadResource(request: ResourceRequest)
    fun clearResourceRequest()
    fun onSourceLanguageClick()
    fun onTargetLanguageClick()
}

class DefaultCreateGlossaryComponent(
    componentContext: ComponentContext,
    private val sharedState: CreateGlossaryStateKeeper,
    private val onNavigateBack: () -> Unit,
    private val onResourceDownloaded: (Resource) -> Unit,
    private val onGlossaryCreated: (Resource, Glossary) -> Unit,
    private val onSelectLanguage: (type: LanguageType) -> Unit
) : CreateGlossaryComponent, KoinComponent, ComponentContext by componentContext {

    private val glossaryRepository: GlossaryRepository by inject()
    private val catalogApi: CatalogApi by inject()
    private val directoryProvider: DirectoryProvider by inject()
    private val resourceContainerAccessor: ResourceContainerAccessor by inject()

    override val model: Value<CreateGlossaryComponent.Model> = sharedState.model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBackClicked() {
        onNavigateBack()
    }

    override fun createGlossary(code: String) {
        componentScope.launch {
            val sourceLanguage = model.value.sourceLanguage ?: return@launch
            val targetLanguage = model.value.targetLanguage ?: return@launch

            sharedState.updateIsSaving(true)

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
                    onGlossaryCreated(resource, glossary.copy(id = it))
                } ?: run {
                    error = getString(Res.string.create_glossary_error)
                }

                sharedState.updateError(error)
            } ?: run {
                sharedState.updateResourceRequest(
                    CreateGlossaryComponent.ResourceRequest(
                        sourceLanguage.slug
                    )
                )
            }

            sharedState.updateIsSaving(false)
        }
    }

    override fun downloadResource(request: CreateGlossaryComponent.ResourceRequest) {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.downloading)
            )

            sharedState.updateProgress(progress)

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
                    onResourceDownloaded(res)
                } else {
                    error = (result as NetworkResult.Error).message
                        ?: getString(Res.string.unknown_error)
                }
            } else {
                error = getString(Res.string.resource_not_found)
            }

            sharedState.updateProgress(null)
            sharedState.updateError(error)
        }
    }

    override fun clearResourceRequest() {
        sharedState.updateResourceRequest(null)
    }

    override fun onSourceLanguageClick() {
        onSelectLanguage(LanguageType.SOURCE)
    }

    override fun onTargetLanguageClick() {
        onSelectLanguage(LanguageType.TARGET)
    }

    private suspend fun findResource(): Resource? {
        return model.value.sourceLanguage?.let { language ->
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