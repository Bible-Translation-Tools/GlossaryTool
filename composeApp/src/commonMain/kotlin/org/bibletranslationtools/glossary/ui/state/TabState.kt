package org.bibletranslationtools.glossary.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.bibletranslationtools.glossary.ui.navigation.MainTab

data class TabState(
    val currentTab: MainTab = MainTab.Read
)

interface TabStateHolder {
    val tabState: StateFlow<TabState>
    fun updateTab(tab: MainTab)
}

class TabStateHolderImpl : TabStateHolder {
    private val _tabState = MutableStateFlow(TabState())
    override val tabState: StateFlow<TabState> = _tabState

    override fun updateTab(tab: MainTab) {
        _tabState.update { current ->
            current.copy(currentTab = tab)
        }
    }
}
