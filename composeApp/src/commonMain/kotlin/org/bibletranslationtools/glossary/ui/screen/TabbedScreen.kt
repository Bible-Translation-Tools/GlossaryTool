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
import dev.burnoo.compose.remembersetting.rememberStringSetting
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

        val resourceState by appStateStore.resourceStateHolder.resourceState
            .collectAsStateWithLifecycle()
        val tabState by appStateStore.tabStateHolder.tabState.collectAsStateWithLifecycle()
        val state by screenModel.state.collectAsStateWithLifecycle()

        var glossaryCode by rememberStringSettingOrNull(
            Settings.GLOSSARY.name
        )
        var resourceId by rememberStringSetting(
            Settings.RESOURCE.name,
            "en_ulb"
        )

        LaunchedEffect(Unit) {
            EventBus.events.receiveAsFlow().collect { event ->
                when (event) {
                    is AppEvent.OpenRef -> {
                        appStateStore.tabStateHolder.updateTab(MainTab.Read)
                        screenModel.loadRef(event.ref)
                    }
                    is AppEvent.SelectGlossary -> {
                        appStateStore.glossaryStateHolder.updateGlossary(event.glossary)
                        appStateStore.tabStateHolder.updateTab(MainTab.Glossary)

                        // saving glossary code to preferences
                        glossaryCode = event.glossary.code
                    }
                    is AppEvent.SelectResource -> {
                        appStateStore.resourceStateHolder.updateResource(event.resource)

                        // saving resource id to preferences
                        resourceId = "${event.resource.lang}_${event.resource.type}"
                    }
                    else -> {}
                }
            }
        }

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
                resourceState.resource?.let { resource ->
                    PhraseDetailsBar(
                        details = phraseDetails,
                        resource = resource,
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
}