package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.init_app
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.domain.InitApp
import org.jetbrains.compose.resources.getString

data class SplashState(
    val initDone: Boolean = false,
    val message: String? = null
)

sealed class SplashEvent {
    data object Idle : SplashEvent()
    data object InitApp : SplashEvent()
}

class SplashScreenModel(
    private val initApp: InitApp
) : ScreenModel {

    private var _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SplashState()
        )

    private val _event: Channel<SplashEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.InitApp -> initializeApp()
            else -> resetChannel()
        }
    }

    fun initializeApp() {
        screenModelScope.launch {
            updateMessage(getString(Res.string.init_app))
            initApp()
            updateInitDone()
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(SplashEvent.Idle)
        }
    }

    private fun updateInitDone() {
        _state.update {
            it.copy(initDone = true)
        }
    }

    private fun updateMessage(message: String?) {
        _state.update {
            it.copy(message = message)
        }
    }
}