package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.exporting_glossary
import glossary.composeapp.generated.resources.glossary_upload_failed
import glossary.composeapp.generated.resources.glossary_uploaded_successfully
import glossary.composeapp.generated.resources.source_text
import glossary.composeapp.generated.resources.uploading_glossary
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.ExportGlossary
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.ImportGlossary
import org.bibletranslationtools.glossary.domain.NetworkResult
import org.bibletranslationtools.glossary.ui.components.UpdateStatus
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class KeyTermsFilter {
    abstract val label: String
    class Chapter(override val label: String) : KeyTermsFilter()
    class SourceText(override val label: String) : KeyTermsFilter()
}

interface KeyTermsIndexComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val glossary: Glossary? = null,
        val allPhrases: List<Phrase> = emptyList(),
        val chapterPhrases: List<Phrase> = emptyList(),
        val filterOptions: List<KeyTermsFilter> = emptyList(),
        val updateStatus: UpdateStatus = UpdateStatus.DEFAULT,
        val snackBarMessage: String? = null,
        val progress: Progress? = null
    )

    fun initialize(glossary: Glossary, book: String, chapter: Int)
    fun navigateImportGlossary()
    fun navigateCreateGlossary()
    fun navigateSearchPhrases()
    fun navigateViewPhrase(phraseId: String)
    fun exportGlossary(file: PlatformFile)
    fun uploadGlossary()
    fun downloadGlossary()
    fun clearHasUpdate()
    fun clearSnackBarMessage()
}

class DefaultKeyTermsIndexComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onNavigateImportGlossary: () -> Unit,
    private val onNavigateCreateGlossary: () -> Unit,
    private val onNavigateSearchPhrases: () -> Unit,
    private val onNavigateViewPhrase: (phraseId: String) -> Unit,
    private val onSelectGlossary: (glossary: Glossary, openKeyTerms: Boolean) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
) : DrawerComponent(componentContext, parentContext), KeyTermsIndexComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()
    private val exportGlossaryUseCase: ExportGlossary by inject()
    private val importGlossaryUseCase: ImportGlossary by inject()
    private val directoryProvider: DirectoryProvider by inject()
    private val glossaryApi: GlossaryApi by inject()

    private val _model = MutableValue(KeyTermsIndexComponent.Model())
    override val model: Value<KeyTermsIndexComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            setFullscreen(false)
            reloadGlossary()
        }
    }

    override fun initialize(glossary: Glossary, book: String, chapter: Int) {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val allPhrases = glossaryRepository.getPhrases(glossary.id)
                .sortedBy { it.phrase.lowercase() }

            val chapterPhrases = allPhrases.filter { phrase ->
                val relevantRef = glossaryRepository.getRefs(phrase.id)
                    .find { ref ->
                        ref.book == book && ref.chapter == chapter.toString()
                    }
                relevantRef != null
            }

            val chapterLabel = "${book.uppercase()} $chapter"
            val options = listOf(
                KeyTermsFilter.Chapter(chapterLabel),
                KeyTermsFilter.SourceText(getString(Res.string.source_text))
            )

            _model.update {
                it.copy(
                    isLoading = false,
                    glossary = glossary,
                    allPhrases = allPhrases,
                    chapterPhrases = chapterPhrases,
                    filterOptions = options
                )
            }

            reloadGlossary()
        }
    }

    override fun navigateImportGlossary() {
        onNavigateImportGlossary()
    }

    override fun navigateCreateGlossary() {
        onNavigateCreateGlossary()
    }

    override fun navigateSearchPhrases() {
        onNavigateSearchPhrases()
    }

    override fun navigateViewPhrase(phraseId: String) {
        onNavigateViewPhrase(phraseId)
    }

    override fun exportGlossary(file: PlatformFile) {
        componentScope.launch {
            _model.value.glossary?.let { glossary ->
                val progress = Progress(
                    value = -1f,
                    message = getString(Res.string.exporting_glossary)
                )
                _model.update { it.copy(progress = progress) }

                withContext(Dispatchers.Default) {
                    exportGlossaryUseCase(glossary, file)
                }

                _model.update { it.copy(progress = null) }
            }
        }
    }

    override fun uploadGlossary() {
        componentScope.launch {
            _model.value.glossary?.let { glossary ->
                val progress = Progress(
                    value = -1f,
                    message = getString(Res.string.uploading_glossary)
                )
                _model.update { it.copy(progress = progress) }

                val message = withContext(Dispatchers.Default) {
                    val uploadPath = directoryProvider.createTempFile("upload", ".zip")
                    val uploadFile = PlatformFile(uploadPath)

                    exportGlossaryUseCase(glossary, uploadFile)

                    if (uploadFile.exists() && uploadFile.size() > 0) {
                        val result = glossaryApi.uploadGlossary(uploadFile)
                        if (result is NetworkResult.Success) {
                            glossaryRepository.setGlossaryVersion(
                                result.data.toLong(),
                                glossary.id!!
                            )
                            getString(Res.string.glossary_uploaded_successfully)
                        } else {
                            println(result)
                            getString(Res.string.glossary_upload_failed)
                        }
                    } else {
                        getString(Res.string.glossary_upload_failed)
                    }
                }

                _model.update { it.copy(progress = null, snackBarMessage = message) }
            }
        }
    }

    override fun downloadGlossary() {
        componentScope.launch {
            _model.value.glossary?.let { glossary ->
                _model.update { it.copy(updateStatus = UpdateStatus.DOWNLOADING) }

                val result: ImportGlossary.Result? = withContext(Dispatchers.IO) {
                    val result = glossaryApi.downloadGlossary(glossary.code)
                    if (result is NetworkResult.Success) {
                        val target = directoryProvider.createTempFile("download", ".zip")
                        directoryProvider.writeFile(result.data, target)

                        if (SystemFileSystem.exists(target)) {
                            importGlossaryUseCase(PlatformFile(target))
                        } else null
                    } else {
                        println(result)
                        null
                    }
                }

                result?.let { (glossary, resource) ->
                    onSelectResource(resource)
                    onSelectGlossary(glossary, false)

                    _model.update { it.copy(updateStatus = UpdateStatus.DOWNLOADED) }
                } ?: run {
                    _model.update { it.copy(updateStatus = UpdateStatus.FAILED) }
                }
            }
        }
    }

    override fun clearHasUpdate() {
        _model.update { it.copy(updateStatus = UpdateStatus.DEFAULT) }
    }

    override fun clearSnackBarMessage() {
        _model.update { it.copy(snackBarMessage = null) }
    }

    private fun reloadGlossary() {
        componentScope.launch {
            _model.value.glossary?.let { glossary ->
                glossaryRepository.getGlossary(glossary.code)?.let { dbGlossary ->
                    _model.update { it.copy(glossary = dbGlossary) }
                }
            }
        }
    }
}