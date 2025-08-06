package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class GlossaryItem(
    val glossary: Glossary,
    val phraseCount: Int,
    val userCount: Int
)

interface GlossaryListComponent : ParentContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val selectedGlossary: GlossaryItem? = null,
        val selectedResource: Resource? = null,
        val glossaries: List<GlossaryItem> = emptyList()
    )
    fun selectGlossary(glossary: GlossaryItem)
    fun navigateImportGlossary()
    fun saveGlossary()
    fun navigateBack()
}

class DefaultGlossaryListComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val onNavigateImportGlossary: () -> Unit,
    private val onSelectGlossary: (glossary: Glossary) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onNavigateBack: () -> Unit
) : AppComponent(componentContext, parentContext),
    GlossaryListComponent, KoinComponent {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()
    private val resourceContainerAccessor: ResourceContainerAccessor by inject()

    private val glossaryState = appStateStore.glossaryStateHolder.glossaryState
    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _model = MutableValue(GlossaryListComponent.Model())
    override val model: Value<GlossaryListComponent.Model> = _model

    init {
        loadGlossaries()
    }

    override fun selectGlossary(glossary: GlossaryItem) {
        componentScope.launch {
            _model.value = _model.value.copy(isLoading = true)

            withContext(Dispatchers.Default) {
                glossaryRepository.getResource(glossary.glossary.resourceId!!)?.let { dbRes ->
                    val resource = resourceContainerAccessor.read(dbRes.filename)
                        ?.copy(id = dbRes.id, url = dbRes.url)

                    _model.update {
                        it.copy(
                            isLoading = false,
                            selectedGlossary = glossary,
                            selectedResource = resource
                        )
                    }
                }
            }
        }
    }

    override fun navigateImportGlossary() {
        onNavigateImportGlossary()
    }

    override fun navigateBack() {
        onNavigateBack()
    }

    override fun saveGlossary() {
        componentScope.launch {
            val glossary = _model.value.selectedGlossary?.glossary ?: return@launch
            val resource = _model.value.selectedResource ?: return@launch

            onSelectResource(resource)
            onSelectGlossary(glossary)
            onNavigateBack()
        }
    }

    private fun loadGlossaries() {
        componentScope.launch {
            _model.update { it.copy(isLoading = true) }

            val glossaries = withContext(Dispatchers.Default) {
                glossaryRepository.getGlossaries()
            }

            val glossaryItems = withContext(Dispatchers.Default) {
                glossaries.map { glossary ->
                    val phraseCount = glossaryRepository.getPhrases(glossary.id).size
                    val userCount = 8 // TODO implement real data
                    GlossaryItem(
                        glossary = glossary,
                        phraseCount = phraseCount,
                        userCount = userCount
                    )
                }
            }

            val selectedGlossary = glossaryItems.singleOrNull {
                it.glossary == glossaryState.value.glossary
            }

            _model.update {
                it.copy(
                    isLoading = false,
                    selectedGlossary = selectedGlossary,
                    glossaries = glossaryItems
                )
            }
        }
    }
}