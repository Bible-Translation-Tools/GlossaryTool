package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.checking_for_updates
import glossary.composeapp.generated.resources.error_checking_updates
import glossary.composeapp.generated.resources.no_updates_found
import glossary.composeapp.generated.resources.updates_found
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.NetworkResult
import org.bibletranslationtools.glossary.toTimestamp
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsIndexComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val progress: Progress? = null,
        val snackBarMessage: String? = null
    )

    fun createGlossary()
    fun viewGlossaries()
    fun checkUpdates()
    fun clearSnackBarMessage()
}

class DefaultSettingsIndexComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onCreateGlossary: () -> Unit,
    private val onViewGlossaries: () -> Unit
) : DrawerComponent(componentContext, parentContext), SettingsIndexComponent, KoinComponent {

    private val glossaryApi: GlossaryApi by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _model = MutableValue(SettingsIndexComponent.Model())
    override val model: Value<SettingsIndexComponent.Model> = _model

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

    override fun checkUpdates() {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.checking_for_updates)
            )
            _model.update { it.copy(progress = progress) }

            val result = with(Dispatchers.Default) {
                val glossaries = glossaryRepository.getGlossaries()
                    .map { glossary ->
                        GlossaryUpdate(
                            id = glossary.id!!,
                            code = glossary.code,
                            version = glossary.version,
                            createdAt = glossary.createdAt.toTimestamp(),
                            updatedAt = glossary.updatedAt.toTimestamp()
                        )
                    }
                val updates = glossaryApi.checkUpdates(glossaries)
                if (updates is NetworkResult.Success) {
                    if (updates.data.isNotEmpty()) {
                        val updatedGlossaries = updates.data
                            .map { (id, code) ->
                                glossaryRepository.setGlossaryHasUpdate(true, id)
                                code
                            }
                            .joinToString(", ")
                        getString(Res.string.updates_found, updatedGlossaries)
                    } else {
                        getString(Res.string.no_updates_found)
                    }
                } else {
                    println((updates as NetworkResult.Error).message)
                    getString(Res.string.error_checking_updates)
                }
            }

            _model.update { it.copy(progress = null, snackBarMessage = result) }
        }
    }

    override fun clearSnackBarMessage() {
        _model.update { it.copy(snackBarMessage = null) }
    }
}