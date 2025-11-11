package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext

interface SettingsIndexComponent : DrawerContext {
    fun createGlossary()
    fun viewGlossaries()
}

class DefaultSettingsIndexComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onCreateGlossary: () -> Unit,
    private val onViewGlossaries: () -> Unit
) : DrawerComponent(componentContext, parentContext), SettingsIndexComponent {

    init {
        doOnResume {
            setFullscreen(false)
        }
    }

    override fun createGlossary() {
        onCreateGlossary()
    }

    override fun viewGlossaries() {
        onViewGlossaries()
    }
}