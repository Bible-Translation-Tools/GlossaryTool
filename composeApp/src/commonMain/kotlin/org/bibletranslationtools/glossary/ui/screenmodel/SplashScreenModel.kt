package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.init_app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.jetbrains.compose.resources.getString

data class SplashState(
    val initDone: Boolean = false,
    val resource: Resource? = null,
    val message: String? = null
)

sealed class SplashEvent {
    data object Idle : SplashEvent()
    data class InitApp(val resource: String) : SplashEvent()
}

class SplashScreenModel(
    private val initApp: InitApp,
    private val workbookDataSource: WorkbookDataSource
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
            is SplashEvent.InitApp -> initializeApp(event.resource)
            else -> resetChannel()
        }
    }

    fun initializeApp(resource: String) {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                message = getString(Res.string.init_app)
            )

            val books = withContext(Dispatchers.IO) {
                initApp()

                withContext(Dispatchers.IO) {
                    workbookDataSource.read(resource)
                }
            }

            _state.value = _state.value.copy(
                initDone = true,
                resource = Resource(resource, books)
            )
        }
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(SplashEvent.Idle)
        }
    }
}