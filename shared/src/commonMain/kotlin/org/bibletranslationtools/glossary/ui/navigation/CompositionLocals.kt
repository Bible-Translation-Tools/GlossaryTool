package org.bibletranslationtools.glossary.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackBarHostState: ProvidableCompositionLocal<SnackbarHostState?> =
    staticCompositionLocalOf { null }
