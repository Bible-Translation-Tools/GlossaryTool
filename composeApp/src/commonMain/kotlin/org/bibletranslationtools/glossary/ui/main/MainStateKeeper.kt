package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

class MainStateKeeper : InstanceKeeper.Instance {
    private val _model = MutableValue(MainComponent.Model())
    val model: Value<MainComponent.Model> = _model

    fun setTriggerUpdate(value: Boolean) {
        _model.update { it.copy(triggerUpdate = value) }
    }

    fun setKeyTermsDrawerOpen(value: Boolean) {
        _model.update { it.copy(keyTermsDrawerOpen = value) }
    }

    fun setSettingsDrawerOpen(value: Boolean) {
        _model.update { it.copy(settingsDrawerOpen = value) }
    }

    override fun onDestroy() {
        // Clean up if needed
    }
}