package org.bibletranslationtools.glossary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.burnoo.compose.remembersetting.rememberStringSetting
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.Locales
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.platform.applyLocale
import org.bibletranslationtools.glossary.ui.DarkColorScheme
import org.bibletranslationtools.glossary.ui.LightColorScheme
import org.bibletranslationtools.glossary.ui.MainAppTheme
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.navigation.LocalAppState
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.screen.SplashScreen
import org.bibletranslationtools.glossary.ui.screen.TabbedScreen
import kotlin.system.exitProcess

class AppState(defaultTab: MainTab) {
    var currentTab by mutableStateOf(defaultTab)
    var resource by mutableStateOf<Resource?>(null)
}

@Composable
fun App() {
    val theme by rememberStringSetting(Settings.THEME.name, Theme.SYSTEM.name)
    val colorScheme = when {
        theme == Theme.LIGHT.name -> LightColorScheme
        theme == Theme.DARK.name -> DarkColorScheme
        theme == Theme.SYSTEM.name && isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }

    val locale by rememberStringSetting(Settings.LOCALE.name, Locales.EN.name)
    applyLocale(locale.lowercase())

    val defaultTab = MainTab.Read
    val snackBarHostState = remember { SnackbarHostState() }
    val appState = remember { AppState(defaultTab) }

    MainAppTheme(colorScheme) {
        Navigator(
            screen = SplashScreen(),
            onBackPressed = {
                if (it is TabbedScreen) {
                    if (appState.currentTab == defaultTab) {
                        exitProcess(0)
                    } else {
                        appState.currentTab = defaultTab
                        false
                    }
                } else true
            }
        ) { navigator ->
            CompositionLocalProvider(LocalRootNavigator provides navigator) {
                CompositionLocalProvider(LocalSnackBarHostState provides snackBarHostState) {
                    CompositionLocalProvider(LocalAppState provides appState) {
                        SlideTransition(navigator)
                    }
                }
            }
        }
    }
}