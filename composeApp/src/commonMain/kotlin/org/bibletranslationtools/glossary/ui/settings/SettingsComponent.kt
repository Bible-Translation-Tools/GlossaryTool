package org.bibletranslationtools.glossary.ui.settings

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent

interface SettingsComponent: ParentContext {
    fun onCreateGlossaryClick()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    val onCreateGlossary: () -> Unit
) : AppComponent(componentContext, parentContext),
    SettingsComponent {

    override fun onCreateGlossaryClick() {
        onCreateGlossary()
    }
}