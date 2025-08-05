package org.bibletranslationtools.glossary.ui.resources

import com.arkivanov.decompose.ComponentContext

interface ResourcesComponent

class DefaultResourcesComponent(
    componentContext: ComponentContext
) : ResourcesComponent, ComponentContext by componentContext {
}