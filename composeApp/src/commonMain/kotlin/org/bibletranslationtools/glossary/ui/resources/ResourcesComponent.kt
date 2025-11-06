package org.bibletranslationtools.glossary.ui.resources

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.AppComponent

interface ResourcesComponent: ParentContext {
    fun showSettingsDrawer()
}

class DefaultResourcesComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext,
    private val onShowSettingsDrawer: () -> Unit
) : AppComponent(componentContext, parentContext), ResourcesComponent {

    override fun showSettingsDrawer() {
        onShowSettingsDrawer()
    }
}