package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface SharedEvent {
    object TriggerUpdate : SharedEvent
}

class MainStateKeeper : InstanceKeeper.Instance {
    private val _model = MutableValue(MainComponent.Model())
    val model: Value<MainComponent.Model> = _model

    private val _event = MutableSharedFlow<SharedEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    fun sendEvent(event: SharedEvent) {
        _event.tryEmit(event)
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