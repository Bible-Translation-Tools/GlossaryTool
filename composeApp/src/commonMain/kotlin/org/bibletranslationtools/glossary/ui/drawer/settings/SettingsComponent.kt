package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.main.DrawerContext

interface SettingsComponent: DrawerContext {
    fun createGlossary()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    private val onCreateGlossary: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    init {
        backHandler.register(
            BackCallback {
                navigateBack()
            }
        )
    }

    override fun createGlossary() {
        onCreateGlossary()
    }

    override fun dismiss() {
        parentContext.dismissDrawer()
    }

    override fun navigateBack() {
        dismiss()
    }
}