package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.importing_glossary
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.ImportGlossary
import org.bibletranslationtools.glossary.ui.AppComponent
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.components.OtpAction
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ImportGlossaryComponent : ParentContext {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val otpCode: List<String?> = (1..5).map { null },
        val focusedIndex: Int? = null,
        val progress: Progress? = null
    )

    fun onOtpAction(action: OtpAction)
    fun onDownloadClicked()
    fun onImportClicked(file: PlatformFile)
}

class DefaultImportGlossaryComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val onSelectGlossary: (glossary: Glossary) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onImportFinished: () -> Unit,
    private val onNavigateBack: () -> Unit
) : AppComponent(componentContext, parentContext),
    ImportGlossaryComponent, KoinComponent {

    private val importGlossary: ImportGlossary by inject()

    private val _model = MutableValue(ImportGlossaryComponent.Model())
    override val model: Value<ImportGlossaryComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun onOtpAction(action: OtpAction) {
        val currentModel = _model.value
        val currentFocusIndex = currentModel.focusedIndex ?: 0

        when (action) {
            is OtpAction.OnChangeFieldFocused -> {
                _model.update { it.copy(focusedIndex = action.index) }
            }

            is OtpAction.OnEnterChar -> {
                val newOtpCode = currentModel.otpCode.toMutableList()
                newOtpCode[action.index] = action.char?.takeIf { it.isNotEmpty() }

                val nextFocusIndex = if (action.char?.isNotEmpty() == true && action.index < newOtpCode.lastIndex) {
                    action.index + 1
                } else {
                    action.index
                }

                _model.update {
                    it.copy(
                        otpCode = newOtpCode,
                        focusedIndex = nextFocusIndex
                    )
                }
            }
            OtpAction.OnKeyboardBack -> {
                val codeAtIndex = currentModel.otpCode.getOrNull(currentFocusIndex)
                val newOtpCode = currentModel.otpCode.toMutableList()
                var previousFocusIndex = currentFocusIndex

                if (codeAtIndex != null) {
                    newOtpCode[currentFocusIndex] = null
                } else if (currentFocusIndex > 0) {
                    previousFocusIndex = currentFocusIndex - 1
                    newOtpCode[previousFocusIndex] = null
                }

                _model.update {
                    it.copy(
                        otpCode = newOtpCode,
                        focusedIndex = previousFocusIndex
                    )
                }
            }
        }
    }

    override fun onDownloadClicked() {
        println(model.value.otpCode)
        if (model.value.otpCode.none { it == null }) {
            val code = model.value.otpCode.joinToString("")
            println("downloading $code...")
        }
    }

    override fun onImportClicked(file: PlatformFile) {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.importing_glossary)
            )
            _model.update { it.copy(progress = progress) }

            val result = withContext(Dispatchers.Default) {
                importGlossary(file)
            }

            onSelectResource(result.resource)
            onSelectGlossary(result.glossary)

            _model.update { it.copy(progress = null) }

            onImportFinished()
        }
    }
}