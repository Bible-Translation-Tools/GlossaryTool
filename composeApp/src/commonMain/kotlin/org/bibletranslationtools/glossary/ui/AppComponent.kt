package org.bibletranslationtools.glossary.ui

import com.arkivanov.decompose.ComponentContext

interface ParentContext {
    fun onBackClick()
}

abstract class AppComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext
) : ParentContext, ComponentContext by componentContext {

    override fun onBackClick() {
        parentContext.onBackClick()
    }
}