package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable

interface FontSetting<T> {
    val name: String
    val value: T

    @Composable
    fun localize(): String
}