package org.bibletranslationtools.glossary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.bibletranslationtools.glossary.ui.screen.SplashScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
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

    MainAppTheme(colorScheme) {
        Navigator(SplashScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}