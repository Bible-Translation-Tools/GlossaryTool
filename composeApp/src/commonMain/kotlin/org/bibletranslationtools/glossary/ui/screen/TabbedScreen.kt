package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.bibletranslationtools.glossary.ui.components.BottomNavBar
import org.bibletranslationtools.glossary.ui.components.KeyboardAware
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedEvent
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

class TabbedScreen : Screen {
    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()
        val screenModel = koinScreenModel<TabbedScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.currentOrThrow

        val navigator = LocalNavigator.currentOrThrow
        val tabState by appStateStore.tabStateHolder.tabState.collectAsStateWithLifecycle()
        val state by screenModel.state.collectAsStateWithLifecycle()

        TabNavigator(tabState.currentTab) { tabNavigator ->
            LaunchedEffect(tabNavigator.current) {
                appStateStore.tabStateHolder.updateTab(tabNavigator.current as MainTab)
            }
            LaunchedEffect(tabState.currentTab) {
                tabNavigator.current = tabState.currentTab
            }

            KeyboardAware {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                    content = { paddingValues ->
                        Surface(
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

            state.phraseDetails?.let { phraseDetails ->
                PhraseDetailsBar(
                    details = phraseDetails,
                    onViewDetails = { phrase ->
                        navigator.push(
                            ViewPhraseScreen(phrase)
                        )
                    },
                    onDismiss = {
                        screenModel.onEvent(
                            TabbedEvent.LoadPhrase(null)
                        )
                    }
                )
            }
        }
    }
}