package org.bibletranslationtools.glossary

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.app_name
import org.bibletranslationtools.glossary.di.initKoin
import org.jetbrains.compose.resources.stringResource

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.app_name),
        ) {
            App()
        }
    }
}