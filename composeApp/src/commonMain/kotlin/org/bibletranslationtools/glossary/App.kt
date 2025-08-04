package org.bibletranslationtools.glossary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.burnoo.compose.remembersetting.rememberStringSetting
import org.bibletranslationtools.glossary.domain.Locales
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.platform.applyLocale
import org.bibletranslationtools.glossary.ui.DarkColorScheme
import org.bibletranslationtools.glossary.ui.LightColorScheme
import org.bibletranslationtools.glossary.ui.MainAppTheme
import org.bibletranslationtools.glossary.ui.main.MainScreen
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.splash.SplashScreen

@Composable
fun App(root: RootComponent) {
    val theme by rememberStringSetting(Settings.THEME, Theme.SYSTEM)
    val colorScheme = when {
        theme == Theme.LIGHT -> LightColorScheme
        theme == Theme.DARK -> DarkColorScheme
        theme == Theme.SYSTEM && isSystemInDarkTheme() -> DarkColorScheme
        else -> LightColorScheme
    }

    val locale by rememberStringSetting(Settings.LOCALE, Locales.EN)
    applyLocale(locale.lowercase())

    val snackBarHostState = remember { SnackbarHostState() }

    MainAppTheme(colorScheme) {
        CompositionLocalProvider(LocalSnackBarHostState provides snackBarHostState) {
            Children(
                stack = root.stack,
                animation = stackAnimation(slide())
            ) {
                when (val child = it.instance) {
                    is RootComponent.Child.Splash -> SplashScreen(child.component)
                    is RootComponent.Child.Main -> MainScreen(child.component)
                }
            }
        }
    }
}