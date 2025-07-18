package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
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
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedEvent
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedScreenModel

class TabbedScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TabbedScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.currentOrThrow

        val navigator = LocalNavigator.currentOrThrow
        val state by screenModel.state.collectAsStateWithLifecycle()

        TabNavigator(MainTab.Read) { tabNavigator ->
            Box {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
                state.phraseDetails?.let { phraseDetails ->
                    PhraseDetailsBar(
                        details = phraseDetails,
                        onViewDetails = {
                            navigator.push(ViewPhraseScreen(phraseDetails))
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
}