package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.create_glossary_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.jetbrains.compose.resources.getString

data class CreateGlossaryState(
    val isSaving: Boolean = false,
    val sourceLanguage: Language? = null,
    val targetLanguage: Language? = null,
    val error: String? = null
)

sealed class CreateGlossaryEvent {
    data object Idle : CreateGlossaryEvent()
    data class OnGlossaryCreated(val glossary: Glossary): CreateGlossaryEvent()
}

class CreateGlossaryScreenModel(
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private var _state = MutableStateFlow(CreateGlossaryState())
    val state: StateFlow<CreateGlossaryState> = _state
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CreateGlossaryState()
        )

    private val _event: Channel<CreateGlossaryEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun updateSourceLanguage(language: Language) {
        _state.update { it.copy(sourceLanguage = language) }
    }

    fun updateTargetLanguage(language: Language) {
        _state.update { it.copy(targetLanguage = language) }
    }

    fun createGlossary(code: String) {
        screenModelScope.launch {
            _state.value.sourceLanguage?.let { sourceLanguage ->
                _state.value.targetLanguage?.let { targetLanguage ->
                    _state.update { it.copy(isSaving = true) }

                    val glossary = Glossary(
                        code = code,
                        author = "User",
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage
                    )

                    val id = withContext(Dispatchers.Default) {
                        glossaryRepository.addGlossary(glossary)
                    }

                    var error: String? = null

                    id?.let {
                        _event.send(CreateGlossaryEvent.OnGlossaryCreated(
                            glossary.copy(id = it)
                        ))
                    } ?: run {
                        error = getString(Res.string.create_glossary_error)
                    }

                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = error
                        )
                    }
                }
            }
        }
    }
}