package org.bibletranslationtools.glossary.ui.resources

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.AppComponent

interface ResourcesComponent: ParentContext

class DefaultResourcesComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext
) : AppComponent(componentContext, parentContext),
    ResourcesComponent