package org.bibletranslationtools.glossary.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import org.bibletranslationtools.glossary.ui.drawer.keyterms.KeyTermsComponent
import org.bibletranslationtools.glossary.ui.drawer.keyterms.KeyTermsScreen
import org.bibletranslationtools.glossary.ui.drawer.settings.SettingsComponent
import org.bibletranslationtools.glossary.ui.drawer.settings.SettingsScreen
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.read.ReadScreen
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(component: MainComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val userState by appStateStore.userStateHolder.state
        .collectAsStateWithLifecycle()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()

    var selectedResource by rememberStringSetting(
        Settings.RESOURCE,
        "en_ulb"
    )
    var selectedGlossaryId by rememberStringSettingOrNull(
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
    var jwtToken by rememberStringSettingOrNull(
        Settings.JWT_TOKEN
    )

    val roundedShape = DrawerDefaults.shape
    val flatShape = RectangleShape

    val drawerSlot by component.drawerSlot.subscribeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerShape by remember(model.fullscreenDrawer) {
        mutableStateOf(
            if (model.fullscreenDrawer) flatShape else roundedShape
        )
    }
    val drawerHasContent = drawerSlot.child?.instance != null

    val snackBarHostState = remember { SnackbarHostState() }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(model.activeResource) {
        model.activeResource?.let { resource ->
            if (resource.toString() != selectedResource) {
                selectedResource = resource.toString()
            }
        }
    }

    LaunchedEffect(model.activeGlossary) {
        model.activeGlossary?.let { glossary ->
            if (glossary.id != selectedGlossaryId) {
                selectedGlossaryId = glossary.id
            }
        }
    }

    LaunchedEffect(Unit) {
        component.verifyLogin(jwtToken)
    }

    LaunchedEffect(userState.user) {
        userState.user?.token?.let { token ->
            jwtToken = token
        } ?: run {
            // If user is null after initialization
            // then we assume user is logged out
            if (initialized) {
                jwtToken = null
            }
            initialized = true
        }
    }

    LaunchedEffect(userState.user, glossaryState.glossary) {
        glossaryState.glossary?.let { glossary ->
            component.getGlossaryUsers(glossary)
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

    CompositionLocalProvider(LocalSnackBarHostState provides snackBarHostState) {
        Box(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerHasContent,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        drawerShape = drawerShape,
                        modifier = Modifier.then(
                            if (model.fullscreenDrawer) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            }
                        )
                    ) {
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
                Children(
                    stack = component.childStack,
                    animation = stackAnimation(Utils.slideHorizontally())
                ) {
                    when (val child = it.instance) {
                        is MainComponent.Child.Read -> ReadScreen(child.component)
                    }
                }
            }
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}