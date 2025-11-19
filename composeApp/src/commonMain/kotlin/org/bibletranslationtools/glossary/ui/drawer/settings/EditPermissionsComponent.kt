package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.koin.core.component.KoinComponent

interface EditPermissionsComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val progress: Progress? = null,
        val snackBarMessage: String? = null
    )
}

class DefaultEditPermissionsComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
) : DrawerComponent(componentContext, parentContext), EditPermissionsComponent, KoinComponent {

    private val _model = MutableValue(EditPermissionsComponent.Model())
    override val model: Value<EditPermissionsComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        doOnResume {
            setFullscreen(true)
        }
    }
}