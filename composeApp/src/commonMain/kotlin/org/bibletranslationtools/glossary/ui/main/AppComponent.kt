package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy

interface ParentContext {
    fun setTopAppBar(slot: ComposableSlot?)
}

abstract class AppComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext
) : ParentContext, ComponentContext by componentContext {

    init {
        parentContext.setTopAppBar(null)

        lifecycle.doOnDestroy {
            parentContext.setTopAppBar(null)
        }
    }

    final override fun setTopAppBar(slot: ComposableSlot?) {
        parentContext.setTopAppBar(slot)
    }
}