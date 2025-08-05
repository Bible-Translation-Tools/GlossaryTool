package org.bibletranslationtools.glossary.ui.settings

import com.arkivanov.decompose.ComponentContext

interface SettingsComponent {
    fun onCreateGlossaryClick()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    val onCreateGlossary: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    override fun onCreateGlossaryClick() {
        onCreateGlossary()
    }
}