package org.bibletranslationtools.glossary.ui.navigation

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator

val LocalRootNavigator: ProvidableCompositionLocal<Navigator?> =
    staticCompositionLocalOf { null }
