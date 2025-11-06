package org.bibletranslationtools.glossary.ui.settings

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.main.DrawerComponent

interface SettingsComponent: DrawerComponent {
    fun createGlossary()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    private val onCreateGlossary: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    override fun createGlossary() {
        onCreateGlossary()
    }

    override fun dismiss() {
        parentContext.dismissDrawer()
    }
}