package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.bibletranslationtools.glossary.ui.components.BottomNavBar
import org.bibletranslationtools.glossary.ui.components.MainTab

class TabbedScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(MainTab.Read) { tabNavigator ->
            Scaffold(
                content = { paddingValues ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        CurrentTab()
                    }
                },
                bottomBar = {
                    BottomNavBar(
                        currentTab = tabNavigator.current as MainTab,
                        onTabSelected = { tab -> tabNavigator.current = tab }
                    )
                }
            )
        }
    }
}