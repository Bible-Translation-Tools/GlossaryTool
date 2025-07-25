package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import org.bibletranslationtools.glossary.ui.navigation.LocalTabScreenState
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedEvent
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedScreenModel

class TabbedScreenState(initialTab: MainTab) {
    var tab by mutableStateOf(initialTab)
}

class TabbedScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TabbedScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.currentOrThrow

        val navigator = LocalNavigator.currentOrThrow
        val screenState = LocalTabScreenState.currentOrThrow
        val state by screenModel.state.collectAsStateWithLifecycle()

        TabNavigator(screenState.tab) { tabNavigator ->
            LaunchedEffect(tabNavigator.current) {
                screenState.tab = tabNavigator.current as MainTab
            }
            LaunchedEffect(screenState.tab) {
                tabNavigator.current = screenState.tab
            }

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
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
                state.phraseDetails?.let { phraseDetails ->
                    PhraseDetailsBar(
                        details = phraseDetails,
                        onViewDetails = { phrase ->
                            navigator.push(
                                ViewPhraseScreen(
                                    phraseDetails.copy(phrase = phrase)
                                )
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
}