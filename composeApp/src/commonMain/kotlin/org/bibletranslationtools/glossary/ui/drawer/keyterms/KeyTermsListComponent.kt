package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.checking_for_updates
import glossary.composeapp.generated.resources.error_checking_updates
import glossary.composeapp.generated.resources.glossary_upload_failed
import glossary.composeapp.generated.resources.glossary_uploaded_successfully
import glossary.composeapp.generated.resources.join_glossary_progress
import glossary.composeapp.generated.resources.join_glossary_success
import glossary.composeapp.generated.resources.no_updates_found
import glossary.composeapp.generated.resources.updates_found
import glossary.composeapp.generated.resources.upload_pending_failed
import glossary.composeapp.generated.resources.upload_pending_success
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
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.NetworkResult
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.domain.usecases.ExportGlossary
import org.bibletranslationtools.glossary.domain.usecases.ImportGlossary
import org.bibletranslationtools.glossary.domain.usecases.MergePendingPhrases
import org.bibletranslationtools.glossary.logE
import org.bibletranslationtools.glossary.toTimestamp
import org.bibletranslationtools.glossary.ui.components.UpdateStatus
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface KeyTermsListComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val phrases: List<Phrase> = emptyList(),
        val updateStatus: UpdateStatus = UpdateStatus.DEFAULT,
        val snackBarMessage: String? = null,
        val progress: Progress? = null
    )

    fun initialize(glossary: Glossary, book: String, chapter: Int)
    fun navigateImportGlossary()
    fun navigateCreateGlossary()
    fun navigateSearchPhrases()
    fun uploadGlossary()
    fun uploadPendingPhrases()
    fun navigateViewPhrase(phraseId: String)
    fun downloadGlossary()
    fun joinGlossary()
    fun checkForUpdates()
    fun clearHasUpdate()
    fun clearSnackBarMessage()
}

class DefaultKeyTermsListComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onNavigateImportGlossary: () -> Unit,
    private val onNavigateCreateGlossary: () -> Unit,
    private val onNavigateSearchPhrases: () -> Unit,
    private val onNavigateViewPhrase: (phraseId: String) -> Unit,
    private val onSelectGlossary: (glossary: Glossary, openKeyTerms: Boolean) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onTriggerUpdate: () -> Unit
) : DrawerComponent(componentContext, parentContext), KeyTermsListComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()
    private val importGlossaryUseCase: ImportGlossary by inject()
    private val directoryProvider: DirectoryProvider by inject()
    private val glossaryApi: GlossaryApi by inject()
    private val exportGlossaryUseCase: ExportGlossary by inject()
    private val mergePendingPhrasesUseCase: MergePendingPhrases by inject()

    private val appState: AppStateStore by inject()
    private val glossaryStateHolder = appState.glossaryStateHolder
    private val glossaryState = glossaryStateHolder.state
    private val resourceState = appState.resourceStateHolder.state

    private val _model = MutableValue(KeyTermsListComponent.Model())
    override val model: Value<KeyTermsListComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            setFullscreen(false)
        }
    }

    override fun initialize(glossary: Glossary, book: String, chapter: Int) {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val phrases = withContext(Dispatchers.Default) {
                val saved = glossaryRepository.getPhrases(glossary.id)
                val pending = glossaryRepository.getPendingPhrases(glossary.id)

                // Overwrite saved phrases with pending ones
                (saved + pending).associateBy { it.id }
                    .values
                    .toList()
                    .sortedBy { it.phrase.lowercase() }
            }

            _model.update {
                it.copy(
                    isLoading = false,
                    phrases = phrases
                )
            }

            loadGlossary()
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

    override fun uploadGlossary() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch

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
                        val newGlossary = glossary.copy(
                            remoteId = result.data.id,
                            version = result.data.version
                        )
                        glossaryRepository.addGlossary(newGlossary)
                        glossaryStateHolder.setGlossary(newGlossary)

                        getString(Res.string.glossary_uploaded_successfully)
                    } else {
                        this@DefaultKeyTermsListComponent.logE("Glossary upload failed: $result")
                        getString(Res.string.glossary_upload_failed)
                    }
                } else {
                    getString(Res.string.glossary_upload_failed)
                }
            }

            _model.update { it.copy(progress = null, snackBarMessage = message) }
        }
    }

    override fun uploadPendingPhrases() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch
            val remoteId = glossary.remoteId ?: return@launch

            val progress = Progress(
                value = -1f,
                message = getString(Res.string.uploading_glossary)
            )
            _model.update { it.copy(progress = progress) }

            val message = withContext(Dispatchers.Default) {
                val result = glossaryApi.uploadPendingPhrases(
                    id = remoteId,
                    phrases = _model.value.phrases.filter { it.pending }
                )
                if (result is NetworkResult.Success) {
                    val message = mergePendingPhrasesUseCase(glossary.id!!)
                    if (!message.success) {
                        this@DefaultKeyTermsListComponent.logE("Error merging pending phrases: ${message.message}")
                    }
                    getString(Res.string.upload_pending_success)
                } else {
                    this@DefaultKeyTermsListComponent.logE("Upload pending phrases failed: $result")
                    getString(Res.string.upload_pending_failed)
                }
            }

            _model.update {
                it.copy(
                    progress = null,
                    snackBarMessage = message,
                    phrases = _model.value.phrases.map { phrase ->
                        if (phrase.pending) {
                            phrase.copy(pending = false)
                        } else phrase
                    }
                )
            }
        }
    }

    override fun navigateViewPhrase(phraseId: String) {
        onNavigateViewPhrase(phraseId)
    }

    override fun downloadGlossary() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch

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
                    this@DefaultKeyTermsListComponent.logE("Download glossary failed: $result")
                    null
                }
            }

            result?.let { (glossary, resource) ->
                onSelectResource(resource)
                onSelectGlossary(glossary, false)
                onTriggerUpdate()

                _model.update { it.copy(updateStatus = UpdateStatus.DOWNLOADED) }
            } ?: run {
                _model.update { it.copy(updateStatus = UpdateStatus.FAILED) }
            }
        }
    }

    override fun joinGlossary() {
        componentScope.launch {
            val remoteId = glossaryState.value.glossary?.remoteId ?: return@launch
            val successMessage = getString(Res.string.join_glossary_success)
            val progressMessage = getString(Res.string.join_glossary_progress)

            _model.update { it.copy(progress = Progress(-1f, progressMessage)) }

            val users = withContext(Dispatchers.Default) {
                glossaryApi.joinGlossary(remoteId).let { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _model.update { it.copy(snackBarMessage = successMessage) }
                            result.data
                        }
                        is NetworkResult.Error -> {
                            _model.update { it.copy(snackBarMessage = result.message.error) }
                            emptyList()
                        }
                    }
                }
            }
            glossaryStateHolder.setUsers(users)
            _model.update { it.copy(progress = null) }
        }
    }

    override fun checkForUpdates() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch
            val remoteId = glossary.remoteId ?: return@launch

            val progress = Progress(
                value = -1f,
                message = getString(Res.string.checking_for_updates)
            )
            _model.update { it.copy(progress = progress) }

            val result = with(Dispatchers.Default) {
                val glossaryUpdate = GlossaryUpdate(
                    id = remoteId,
                    version = glossary.version,
                    createdAt = glossary.createdAt.toTimestamp(),
                    updatedAt = glossary.updatedAt.toTimestamp()
                )

                val updates = glossaryApi.checkUpdates(listOf(glossaryUpdate))
                if (updates is NetworkResult.Success) {
                    if (updates.data.any { it.id == glossary.remoteId }) {
                        glossaryStateHolder.setGlossary(glossary.copy(hasUpdate = true))
                        getString(Res.string.updates_found)
                    } else {
                        getString(Res.string.no_updates_found)
                    }
                } else {
                    this@DefaultKeyTermsListComponent.logE("Check for updates failed: ${(updates as NetworkResult.Error).message}")
                    getString(Res.string.error_checking_updates)
                }
            }

            _model.update { it.copy(progress = null, snackBarMessage = result) }
        }
    }

    override fun clearHasUpdate() {
        _model.update { it.copy(updateStatus = UpdateStatus.DEFAULT) }
        glossaryState.value.glossary?.let {
            glossaryStateHolder.setGlossary(it.copy(hasUpdate = false))
        }
    }

    override fun clearSnackBarMessage() {
        _model.update { it.copy(snackBarMessage = null) }
    }

    private fun loadGlossary() {
        componentScope.launch {
            val glossary = glossaryState.value.glossary ?: return@launch
            val glossaryId = glossary.id ?: return@launch

            withContext(Dispatchers.Default) {
                glossaryRepository.getGlossary(glossaryId)
            }?.let { dbGlossary ->
                glossaryStateHolder.setGlossary(
                    dbGlossary.copy(hasUpdate = glossary.hasUpdate)
                )
            }
        }
    }

    private fun findRelevantRefs(
        phrase: Phrase,
        book: String,
        chapter: Int
    ): List<Ref> {
        val resource = resourceState.value.resource ?: return emptyList()

        val regex = Regex(
            pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
            option = RegexOption.IGNORE_CASE
        )
        val refs = mutableListOf<Ref>()
        val verses = resource.books.singleOrNull {
            book == it.slug
        }
            ?.chapters?.singleOrNull {
                chapter == it.number
            }?.verses ?: return emptyList()

        for (verse in verses) {
            val matchCount = regex.findAll(verse.text).count()
            if (matchCount > 0) {
                repeat(matchCount) {
                    refs.add(
                        Ref(
                            book = book,
                            chapter = chapter.toString(),
                            verse = verse.number,
                            phraseId = phrase.id
                        )
                    )
                }
            }
        }
        return refs
    }
}