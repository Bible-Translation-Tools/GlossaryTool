package org.bibletranslationtools.glossary.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator
import org.bibletranslationtools.glossary.ui.screen.TabbedScreenState

val LocalSnackBarHostState: ProvidableCompositionLocal<SnackbarHostState?> =
    staticCompositionLocalOf { null }

val LocalRootNavigator: ProvidableCompositionLocal<Navigator?> =
    staticCompositionLocalOf { null }

val LocalTabScreenState = staticCompositionLocalOf<TabbedScreenState?> { null }