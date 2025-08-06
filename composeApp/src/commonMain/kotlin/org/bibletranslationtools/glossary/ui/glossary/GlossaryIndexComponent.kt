package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.exporting_glossary
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.domain.ExportGlossary
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GlossaryIndexComponent : ParentContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val phrases: List<Phrase> = emptyList(),
        val progress: Progress? = null
    )

    fun loadPhrases(glossary: Glossary)
    fun navigateImportGlossary()
    fun navigateGlossaryList()
    fun navigateSearchPhrases()
    fun navigateViewPhrase(phrase: Phrase)
    fun onExportGlossaryClicked(glossary: Glossary, file: PlatformFile)
}

class DefaultGlossaryIndexComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val onNavigateImportGlossary: () -> Unit,
    private val onNavigateGlossaryList: () -> Unit,
    private val onNavigateSearchPhrases: () -> Unit,
    private val onNavigateViewPhrase: (phrase: Phrase) -> Unit
) : AppComponent(componentContext, parentContext),
    GlossaryIndexComponent, KoinComponent {

    private val glossaryRepository: GlossaryRepository by inject()
    private val exportGlossary: ExportGlossary by inject()

    private val _model = MutableValue(GlossaryIndexComponent.Model())
    override val model: Value<GlossaryIndexComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun loadPhrases(glossary: Glossary) {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val phrases = withContext(Dispatchers.Default) {
                glossaryRepository.getPhrases(glossary.id)
            }

            _model.update {
                it.copy(
                    isLoading = false,
                    phrases = phrases
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

    override fun navigateViewPhrase(phrase: Phrase) {
        onNavigateViewPhrase(phrase)
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
}