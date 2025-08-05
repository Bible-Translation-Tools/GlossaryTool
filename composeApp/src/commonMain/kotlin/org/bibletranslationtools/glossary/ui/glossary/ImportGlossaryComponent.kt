package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
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
import org.bibletranslationtools.glossary.ui.components.OtpAction
import org.bibletranslationtools.glossary.ui.main.ComposableSlot
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ImportGlossaryComponent {
    val model: Value<Model>

    data class Model(
        val isLoading: Boolean = false,
        val otpCode: List<String?> = (1..5).map { null },
        val focusedIndex: Int? = null,
        val isOtpValid: Boolean? = null,
        val progress: Progress? = null
    )

    fun onBackClicked()
    fun onOtpAction(action: OtpAction)
    fun onImportClicked(file: PlatformFile)
    fun setTopBar(slot: ComposableSlot?)
}

class DefaultImportGlossaryComponent(
    componentContext: ComponentContext,
    private val onSelectGlossary: (glossary: Glossary) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onImportFinished: () -> Unit,
    private val onNavigateBack: () -> Unit,
    private val onSetTopBar: (ComposableSlot?) -> Unit
) : ImportGlossaryComponent, KoinComponent, ComponentContext by componentContext {

    private val importGlossary: ImportGlossary by inject()

    private val _model = MutableValue(ImportGlossaryComponent.Model())
    override val model: Value<ImportGlossaryComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        lifecycle.doOnDestroy {
            setTopBar(null)
        }
    }

    override fun onBackClicked() {
        onNavigateBack()
    }

    override fun onOtpAction(action: OtpAction) {
        when (action) {
            is OtpAction.OnChangeFieldFocused -> {
                _model.update {
                    it.copy(
                        focusedIndex = action.index
                    )
                }
            }

            is OtpAction.OnEnterChar -> {
                enterChar(action.char, action.index)
            }

            OtpAction.OnKeyboardBack -> {
                val previousIndex = getPreviousFocusedIndex(_model.value.focusedIndex)
                _model.update {
                    it.copy(
                        otpCode = it.otpCode.mapIndexed { index, number ->
                            if (index == previousIndex) {
                                null
                            } else {
                                number
                            }
                        },
                        focusedIndex = previousIndex
                    )
                }
            }
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

    override fun setTopBar(slot: ComposableSlot?) {
        onSetTopBar(slot)
    }

    private fun enterChar(char: String?, index: Int) {
        val newCode = _model.value.otpCode.mapIndexed { currentIndex, currentChar ->
            if (currentIndex == index && char?.any { it.isLetterOrDigit() } == true) {
                char
            } else {
                currentChar
            }
        }
        val wasCharRemoved = char == null
        _model.update {
            it.copy(
                otpCode = newCode,
                focusedIndex = if (wasCharRemoved || it.otpCode.getOrNull(index) != null) {
                    it.focusedIndex
                } else {
                    getNextFocusedTextFieldIndex(
                        currentCode = it.otpCode,
                        currentFocusedIndex = it.focusedIndex
                    )
                }
            )
        }
    }

    private fun getPreviousFocusedIndex(currentIndex: Int?): Int? {
        return currentIndex?.minus(1)?.coerceAtLeast(0)
    }

    private fun getNextFocusedTextFieldIndex(
        currentCode: List<String?>,
        currentFocusedIndex: Int?
    ): Int? {
        if (currentFocusedIndex == null) {
            return null
        }

        if (currentFocusedIndex == 4) {
            return currentFocusedIndex
        }

        return getFirstEmptyFieldIndexAfterFocusedIndex(
            code = currentCode,
            currentFocusedIndex = currentFocusedIndex
        )
    }

    private fun getFirstEmptyFieldIndexAfterFocusedIndex(
        code: List<String?>,
        currentFocusedIndex: Int
    ): Int {
        code.forEachIndexed { index, number ->
            if (index <= currentFocusedIndex) {
                return@forEachIndexed
            }
            if (number == null) {
                return index
            }
        }
        return currentFocusedIndex
    }
}