package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.BottomNavBar
import org.bibletranslationtools.glossary.ui.components.KeyboardAware
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.event.AppEvent
import org.bibletranslationtools.glossary.ui.event.EventBus
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.screenmodel.SharedScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

class TabbedScreen : Screen {

    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = navigator.koinNavigatorScreenModel<SharedScreenModel>()
        val snackBarHostState = LocalSnackBarHostState.currentOrThrow

        val tabState by appStateStore.tabStateHolder.tabState.collectAsStateWithLifecycle()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val appEvent by EventBus.events.receiveAsFlow()
            .collectAsStateWithLifecycle(AppEvent.Idle)

        var glossaryCode by rememberStringSettingOrNull(
            Settings.GLOSSARY.name
        )

        TabNavigator(tabState.currentTab) { tabNavigator ->
            LaunchedEffect(tabNavigator.current) {
                appStateStore.tabStateHolder.updateTab(tabNavigator.current as MainTab)
            }
            LaunchedEffect(tabState.currentTab) {
                tabNavigator.current = tabState.currentTab
            }
            LaunchedEffect(appEvent) {
                when (appEvent) {
                    is AppEvent.OpenRef -> {
                        appStateStore.tabStateHolder.updateTab(MainTab.Read)

                        val ref = (appEvent as AppEvent.OpenRef).ref
                        screenModel.loadRef(ref)
                    }
                    is AppEvent.SelectGlossary -> {
                        val glossary = (appEvent as AppEvent.SelectGlossary).glossary

                        appStateStore.glossaryStateHolder.updateGlossary(glossary)
                        appStateStore.tabStateHolder.updateTab(MainTab.Glossary)

                        // saving glossary code to preferences
                        glossaryCode = glossary.code
                    }
                    else -> {}
                }
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
                    onNavPhrase = { screenModel.navigatePhrase(it) },
                    onNavRef = { screenModel.navigateRef(it) },
                    onViewDetails = { phrase ->
                        navigator.push(
                            ViewPhraseScreen(phrase)
                        )
                    },
                    onDismiss = {
                        screenModel.clearPhraseDetails()
                    }
                )
            }
        }
    }
}