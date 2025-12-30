package org.bibletranslationtools.glossary.ui.drawer.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnResume
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.login_progress
import glossary.composeapp.generated.resources.login_success
import glossary.composeapp.generated.resources.updating_emoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Progress
import org.bibletranslationtools.glossary.data.api.PendingPhrase
import org.bibletranslationtools.glossary.data.api.User
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.NetworkResult
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
    fun editPermissions()
    fun clearSnackBarMessage()
    fun loadPendingPhrases(glossary: Glossary)
    fun reviewChanges()
    fun updateEmoji(emoji: String)
}

class DefaultSettingsListComponent(
    componentContext: ComponentContext,
    parentContext: DrawerContext,
    private val onCreateGlossary: () -> Unit,
    private val onViewGlossaries: () -> Unit,
    private val onUserUpdated: (User) -> Unit,
    private val onLogout: () -> Unit,
    private val onEditPermissions: () -> Unit,
    private val onReviewChanges: () -> Unit
) : DrawerComponent(componentContext, parentContext), SettingsListComponent, KoinComponent {

    private val glossaryApi: GlossaryApi by inject()

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
                            onUserUpdated(result.data)
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

    override fun updateEmoji(emoji: String) {
        componentScope.launch {
            val progress = Progress(
                value = -1f,
                message = getString(Res.string.updating_emoji)
            )
            _model.update { it.copy(progress = progress) }

            val result = withContext(Dispatchers.Default) {
                glossaryApi.updateEmoji(emoji)
            }
            when (result) {
                is NetworkResult.Success -> {
                    onUserUpdated(result.data)
                }
                is NetworkResult.Error -> {
                    println(result.message)
                    _model.update { it.copy(snackBarMessage = result.message.error) }
                }
            }

            _model.update { it.copy(progress = null) }
        }
    }
}