package org.bibletranslationtools.glossary.ui

import com.arkivanov.decompose.ComponentContext

interface ParentContext {
    fun onBackClick()
    fun dismissDrawer()
    fun openSettings()
    fun openKeyTerms()
}

abstract class AppComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext
) : ParentContext, ComponentContext by componentContext {

    override fun onBackClick() {
        parentContext.onBackClick()
    }

    override fun dismissDrawer() {
        parentContext.dismissDrawer()
    }

    override fun openSettings() {
        parentContext.openSettings()
    }

    override fun openKeyTerms() {
        parentContext.openKeyTerms()
    }
}