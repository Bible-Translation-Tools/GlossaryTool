package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.exporting_glossary
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
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.ExportGlossary
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.NetworkResult
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
        val allPhrases: List<Phrase> = emptyList(),
        val chapterPhrases: List<Phrase> = emptyList(),
        val filterOptions: List<KeyTermsFilter> = emptyList(),
        val progress: Progress? = null
    )

    fun loadPhrases(glossary: Glossary)
    fun navigateImportGlossary()
    fun navigateGlossaryList()
    fun navigateSearchPhrases()
    fun navigateViewPhrase(phraseId: String)
    fun onExportGlossaryClicked(glossary: Glossary, file: PlatformFile)
    fun onUploadGlossaryClicked(glossary: Glossary)
}

class DefaultKeyTermsIndexComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val book: Workbook,
    private val chapter: Chapter,
    private val onNavigateImportGlossary: () -> Unit,
    private val onNavigateGlossaryList: () -> Unit,
    private val onNavigateSearchPhrases: () -> Unit,
    private val onNavigateViewPhrase: (phraseId: String) -> Unit
) : DrawerComponent(componentContext, parentContext), KeyTermsIndexComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()
    private val exportGlossary: ExportGlossary by inject()
    private val directoryProvider: DirectoryProvider by inject()
    private val glossaryApi: GlossaryApi by inject()

    private val _model = MutableValue(KeyTermsIndexComponent.Model())
    override val model: Value<KeyTermsIndexComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadPhrases(glossary: Glossary) {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val allPhrases = glossaryRepository.getPhrases(glossary.id)
                .sortedBy { it.phrase.lowercase() }

            val chapterPhrases = allPhrases.filter { phrase ->
                val relevantRef = glossaryRepository.getRefs(phrase.id)
                    .find { ref ->
                        ref.book == book.slug
                                && ref.chapter == chapter.number.toString()
                    }
                relevantRef != null
            }

            val chapterLabel = "${book.title} ${chapter.number}"
            val options = listOf(
                KeyTermsFilter.Chapter(chapterLabel),
                KeyTermsFilter.SourceText(getString(Res.string.source_text))
            )

            _model.update {
                it.copy(
                    isLoading = false,
                    allPhrases = allPhrases,
                    chapterPhrases = chapterPhrases,
                    filterOptions = options
                )
            }
        }
    }

    override fun navigateImportGlossary() {
        onNavigateImportGlossary()
    }

    override fun navigateGlossaryList() {
        onNavigateGlossaryList()
    }

    override fun navigateSearchPhrases() {
        onNavigateSearchPhrases()
    }

    override fun navigateViewPhrase(phraseId: String) {
        onNavigateViewPhrase(phraseId)
    }

    override fun onExportGlossaryClicked(glossary: Glossary, file: PlatformFile) {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.exporting_glossary)
            )
            _model.update { it.copy(progress = progress) }

            withContext(Dispatchers.Default) {
                exportGlossary(glossary, file)
            }

            _model.update { it.copy(progress = null) }
        }
    }

    override fun onUploadGlossaryClicked(glossary: Glossary) {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.uploading_glossary)
            )
            _model.update { it.copy(progress = progress) }

            val uploadPath = directoryProvider.createTempFile("upload", ".zip")
            val uploadFile = PlatformFile(uploadPath)

            exportGlossary(glossary, uploadFile)

            if (uploadFile.exists() && uploadFile.size() > 0) {
                val result = glossaryApi.uploadGlossary(uploadFile)
                if (result is NetworkResult.Success) {
                    println("Upload successful")
                } else {
                    println("Upload failed")
                    println(result)
                }
            }

            _model.update { it.copy(progress = null) }
        }
    }
}