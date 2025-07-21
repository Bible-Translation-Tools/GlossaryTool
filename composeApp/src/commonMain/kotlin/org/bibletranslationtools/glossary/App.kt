package org.bibletranslationtools.glossary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.burnoo.compose.remembersetting.rememberStringSetting
import org.bibletranslationtools.glossary.domain.Locales
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.platform.applyLocale
import org.bibletranslationtools.glossary.ui.DarkColorScheme
import org.bibletranslationtools.glossary.ui.LightColorScheme
import org.bibletranslationtools.glossary.ui.MainAppTheme
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.screen.SplashScreen
import org.bibletranslationtools.glossary.ui.screen.TabbedScreen

@Composable
fun App() {
    val theme by rememberStringSetting(Settings.THEME.name, Theme.SYSTEM.name)
    val colorScheme = when (theme) {
        Theme.LIGHT.name -> LightColorScheme
        Theme.DARK.name -> DarkColorScheme
        Theme.SYSTEM.name if isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }

    val locale by rememberStringSetting(Settings.LOCALE.name, Locales.EN.name)
    applyLocale(locale.lowercase())

    val snackBarHostState = remember { SnackbarHostState() }

    MainAppTheme(colorScheme) {
        Navigator(
            screen = SplashScreen(),
            onBackPressed = { it !is TabbedScreen }
        ) { navigator ->
            CompositionLocalProvider(LocalRootNavigator provides navigator) {
                CompositionLocalProvider(LocalSnackBarHostState provides snackBarHostState) {
                    SlideTransition(navigator)
                }
            }
        }
    }
}