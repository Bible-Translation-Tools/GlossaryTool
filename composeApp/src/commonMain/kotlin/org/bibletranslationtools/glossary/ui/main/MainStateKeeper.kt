package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

class MainStateKeeper : InstanceKeeper.Instance {
    private val _model = MutableValue(MainComponent.Model())
    val model: Value<MainComponent.Model> = _model

    fun updatePhraseUpdated(value: Boolean) {
        _model.update { it.copy(phraseUpdated = value) }
    }

    fun updateKeyTermsDrawerOpen(value: Boolean) {
        _model.update { it.copy(keyTermsDrawerOpen = value) }
    }

    fun updateSettingsDrawerOpen(value: Boolean) {
        _model.update { it.copy(settingsDrawerOpen = value) }
    }

    override fun onDestroy() {
        // Clean up if needed
    }
}