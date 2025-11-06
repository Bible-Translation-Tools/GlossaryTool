package org.bibletranslationtools.glossary.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.platform.showStatusBars
import org.bibletranslationtools.glossary.ui.components.PhraseDetailsBar
import org.bibletranslationtools.glossary.ui.glossary.GlossaryScreen
import org.bibletranslationtools.glossary.ui.glossary.KeyTermsComponent
import org.bibletranslationtools.glossary.ui.glossary.KeyTermsScreen
import org.bibletranslationtools.glossary.ui.read.ReadScreen
import org.bibletranslationtools.glossary.ui.resources.ResourcesScreen
import org.bibletranslationtools.glossary.ui.settings.SettingsComponent
import org.bibletranslationtools.glossary.ui.settings.SettingsScreen
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(component: MainComponent) {
    val model by component.model.subscribeAsState()

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

    val drawerSlot by component.drawerSlot.subscribeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

    LaunchedEffect(drawerSlot.child) {
        if (drawerSlot.child != null) {
            showStatusBars(false)
            drawerState.open()
        } else {
            showStatusBars(true)
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed && drawerSlot.child != null) {
            component.dismissDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0, 24, 0, 32)
            ) {
                val drawerSlot by component.drawerSlot.subscribeAsState()
                drawerSlot.child?.instance?.let { component ->
                    when (component) {
                        is SettingsComponent -> {
                            SettingsScreen(component)
                        }
                        is KeyTermsComponent -> {
                            KeyTermsScreen(component)
                        }
                    }
                }
            }
        },
        scrimColor = Color(0x800F2F4C)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Children(
                stack = component.childStack,
                animation = stackAnimation(Utils.slideHorizontally()),
                modifier = Modifier.padding(paddingValues)
            ) {
                when (val child = it.instance) {
                    is MainComponent.Child.Read -> ReadScreen(child.component)
                    is MainComponent.Child.Glossary -> GlossaryScreen(child.component)
                    is MainComponent.Child.Resources -> ResourcesScreen(child.component)
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