package org.bibletranslationtools.glossary.ui.read

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface BrowseComponent: ParentContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val book: Workbook? = null,
        val chapter: Chapter? = null,
        val books: List<Workbook> = emptyList()
    )

    fun onBackClick()
    fun onRefClick(ref: RefOption)
}

class DefaultBrowseComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val book: String,
    private val chapter: Int,
    private val onNavigateBack: () -> Unit,
    private val onNavigateRef: (RefOption) -> Unit
) : AppComponent(componentContext, parentContext),
    BrowseComponent, KoinComponent {

    private val appStateStore: AppStateStore by inject()

    private val _model = MutableValue(BrowseComponent.Model())
    override val model: Value<BrowseComponent.Model> = _model

    private val resourceState = appStateStore.resourceStateHolder.resourceState

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        componentScope.launch {
            val resource = resourceState.value.resource ?: return@launch
            _model.update { it.copy(isLoading = true) }

            withContext(Dispatchers.Default) {
                val books = resource.books
                val activeBook = books.firstOrNull { it.slug == book }
                val activeChapter = activeBook?.chapters?.firstOrNull { it.number == chapter }

                _model.update {
                    it.copy(
                        isLoading = false,
                        books = books,
                        book = activeBook,
                        chapter = activeChapter
                    )
                }
            }
        }
    }

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun onRefClick(ref: RefOption) {
        onNavigateRef(ref)
    }
}