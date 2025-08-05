package org.bibletranslationtools.glossary.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.BottomNavBar
import org.bibletranslationtools.glossary.ui.components.KeyboardAware
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.glossary.GlossaryScreen
import org.bibletranslationtools.glossary.ui.read.ReadScreen
import org.bibletranslationtools.glossary.ui.resources.ResourcesScreen
import org.bibletranslationtools.glossary.ui.settings.SettingsScreen
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(component: MainComponent) {
    val model by component.model.subscribeAsState()
    val topBarContent by component.topBarSlot.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val resourceState by appStateStore.resourceStateHolder.resourceState
        .collectAsStateWithLifecycle()

    val childStack by component.childStack.subscribeAsState()
    val activeChild = childStack.active.instance

    var selectedResource by rememberStringSetting(
        Settings.RESOURCE,
        "en_ulb"
    )
    var selectedGlossary by rememberStringSettingOrNull(
        Settings.GLOSSARY
    )
    var activeBookSlug by rememberStringSetting(
        Settings.BOOK,
        "mat"
    )
    var activeChapterNum by rememberIntSetting(
        Settings.CHAPTER,
        1
    )

    LaunchedEffect(model.activeResource) {
        model.activeResource?.let { resource ->
            if (resource.toString() != selectedResource) {
                selectedResource = resource.toString()
            }
        }
    }

    LaunchedEffect(model.activeGlossary) {
        model.activeGlossary?.let { glossary ->
            if (glossary.code != selectedGlossary) {
                selectedGlossary = glossary.code
            }
        }
    }

    KeyboardAware {
        Scaffold(
            topBar = {
                topBarContent()
            },
            bottomBar = {
                BottomNavBar(activeChild) { tab ->
                    component.onTabClicked(tab)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Children(
                    stack = component.childStack,
                    animation = stackAnimation(fade())
                ) {
                    when (val child = it.instance) {
                        is MainComponent.Child.Read -> ReadScreen(child.component)
                        is MainComponent.Child.Glossary -> GlossaryScreen(child.component)
                        is MainComponent.Child.Resources -> ResourcesScreen(child.component)
                        is MainComponent.Child.Settings -> SettingsScreen(child.component)
                    }
                }
            }
        }
    }

    model.phraseDetails?.let { phraseDetails ->
        resourceState.resource?.let { resource ->
            PhraseDetailsBar(
                details = phraseDetails,
                resource = resource,
                onNavPhrase = { component.navigatePhrase(it) },
                onNavRef = { component.navigateRef(it) },
                onViewDetails = { phrase ->
                    component.onViewPhraseClick(phrase)
                },
                onDismiss = {
                    component.clearPhraseDetails()
                }
            )
        }
    }
}