package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.checking_for_updates
import glossary.composeapp.generated.resources.error_checking_updates
import glossary.composeapp.generated.resources.login_progress
import glossary.composeapp.generated.resources.login_success
import glossary.composeapp.generated.resources.no_updates_found
import glossary.composeapp.generated.resources.updates_found
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.api.GlossaryUpdate
import org.bibletranslationtools.glossary.data.api.PendingPhrase
import org.bibletranslationtools.glossary.data.api.User
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.NetworkResult
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import org.bibletranslationtools.glossary.toTimestamp
import org.bibletranslationtools.glossary.ui.drawer.DrawerComponent
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsListComponent : DrawerContext {
    val model: Value<Model>

    data class Model(
        val progress: Progress? = null,
        val snackBarMessage: String? = null,
        val pendingPhrases: List<PendingPhrase> = emptyList(),
        val pendingPhrasesLoading: Boolean = false
    )

    fun login(username: String, password: String)
    fun logout()
    fun createGlossary()
    fun viewGlossaries()
    fun checkUpdates()
    fun editPermissions()
    fun clearSnackBarMessage()
    fun loadPendingPhrases(glossary: Glossary)
    fun reviewChanges()
}

class DefaultSettingsListComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onCreateGlossary: () -> Unit,
    private val onViewGlossaries: () -> Unit,
    private val onLogin: (User) -> Unit,
    private val onLogout: () -> Unit,
    private val onEditPermissions: () -> Unit,
    private val onReviewChanges: () -> Unit
) : DrawerComponent(componentContext, parentContext), SettingsListComponent, KoinComponent {

    private val glossaryApi: GlossaryApi by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _model = MutableValue(SettingsListComponent.Model())
    override val model: Value<SettingsListComponent.Model> = _model

    init {
        doOnResume {
            setFullscreen(false)
        }
    }

    override fun login(username: String, password: String) {
        componentScope.launch {
            val loginProgress = getString(Res.string.login_progress)
            val success = getString(Res.string.login_success)

            _model.update {
                it.copy(
                    progress = Progress(value = -1f, message = loginProgress)
                )
            }

            withContext(Dispatchers.Default) {
                glossaryApi.login(username, password).let { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            onLogin(result.data)
                            _model.update {
                                it.copy(snackBarMessage = success)
                            }
                        }
                        is NetworkResult.Error -> {
                            _model.update {
                                it.copy(snackBarMessage = result.message.error)
                            }
                        }
                    }
                }
            }

            _model.update { it.copy(progress = null) }
        }
    }

    override fun logout() {
        onLogout()
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

    override fun editPermissions() {
        onEditPermissions()
    }

    override fun loadPendingPhrases(glossary: Glossary) {
        componentScope.launch {
            _model.update { it.copy(pendingPhrasesLoading = true) }
            val result = withContext(Dispatchers.Default) {
                glossaryApi.getPendingPhrases(glossary.code)
            }
            when (result) {
                is NetworkResult.Success -> {
                    _model.update {
                        it.copy(pendingPhrases = result.data)
                    }
                }
                is NetworkResult.Error -> {
                    println(result)
                }
            }
            _model.update { it.copy(pendingPhrasesLoading = false) }
        }
    }

    override fun clearSnackBarMessage() {
        _model.update { it.copy(snackBarMessage = null) }
    }

    override fun reviewChanges() {
        onReviewChanges()
    }
}