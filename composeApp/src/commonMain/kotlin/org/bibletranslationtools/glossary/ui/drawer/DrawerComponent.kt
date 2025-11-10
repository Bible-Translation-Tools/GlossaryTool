package org.bibletranslationtools.glossary.ui.drawer

import com.arkivanov.decompose.ComponentContext

interface DrawerContext {
    fun dismiss()
    fun navigateBack()
    fun setFullscreen(fullscreen: Boolean)
}

abstract class DrawerComponent(
    componentContext: ComponentContext,
    private val parentContext: DrawerContext
) : DrawerContext, ComponentContext by componentContext {

    override fun dismiss() {
        parentContext.dismiss()
    }

    override fun navigateBack() {
        parentContext.navigateBack()
    }

    override fun setFullscreen(fullscreen: Boolean) {
        parentContext.setFullscreen(fullscreen)
    }
}