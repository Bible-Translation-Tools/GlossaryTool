package org.bibletranslationtools.glossary.ui.resources

import com.arkivanov.decompose.ComponentContext
import org.bibletranslationtools.glossary.ui.AppComponent
import org.bibletranslationtools.glossary.ui.ParentContext

interface ResourcesComponent: ParentContext

class DefaultResourcesComponent(
    componentContext: ComponentContext,
    parentContext: ParentContext
) : AppComponent(componentContext, parentContext), ResourcesComponent