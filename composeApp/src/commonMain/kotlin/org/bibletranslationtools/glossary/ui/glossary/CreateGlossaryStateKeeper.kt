package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Progress

class CreateGlossaryStateKeeper : InstanceKeeper.Instance {
    private val _model = MutableValue(CreateGlossaryComponent.Model())
    val model: Value<CreateGlossaryComponent.Model> = _model

    fun updateIsSaving(value: Boolean) {
        _model.update { it.copy(isSaving = value) }
    }

    fun updateError(message: String?) {
        _model.update { it.copy(error = message) }
    }

    fun updateResourceRequest(request: CreateGlossaryComponent.ResourceRequest?) {
        _model.update { it.copy(resourceRequest = request) }
    }

    fun updateProgress(progress: Progress?) {
        _model.update { it.copy(progress = progress) }
    }

    fun onLanguageSelected(type: LanguageType, language: Language) {
        when (type) {
            LanguageType.SOURCE -> _model.update { it.copy(sourceLanguage = language) }
            LanguageType.TARGET -> _model.update { it.copy(targetLanguage = language) }
        }
    }

    override fun onDestroy() {
        // Clean up if needed
    }
}