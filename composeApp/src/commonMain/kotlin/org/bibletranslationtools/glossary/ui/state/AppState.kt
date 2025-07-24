package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.navigation.MainTab

data class AppState(
    val resource: Resource? = null,
    val currentTab: MainTab = MainTab.Read
)

interface AppStateHolder {
    val appState: StateFlow<AppState>
    fun updateResource(resource: Resource)
    fun updateTab(tab: MainTab)
}

class AppStateHolderImpl : AppStateHolder {
    private val _appState = MutableStateFlow(AppState())
    override val appState: StateFlow<AppState> = _appState

    override fun updateResource(resource: Resource) {
        _appState.update { current ->
            current.copy(resource = resource)
        }
    }

    override fun updateTab(tab: MainTab) {
        _appState.update { current ->
            current.copy(currentTab = tab)
        }
    }
}
