package org.bibletranslationtools.glossary

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.bibletranslationtools.glossary.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    document.body?.let {
        ComposeViewport(it) {
            App()
        }
    }
}