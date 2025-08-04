package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface ImportGlossaryComponent {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false
    )

    fun onBackClicked()
}

class DefaultImportGlossaryComponent(
    componentContext: ComponentContext,
    private val onNavigateBack: () -> Unit,
) : ImportGlossaryComponent, ComponentContext by componentContext {

    private val _model = MutableValue(ImportGlossaryComponent.Model())
    override val model: Value<ImportGlossaryComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBackClicked() {
        onNavigateBack()
    }
}