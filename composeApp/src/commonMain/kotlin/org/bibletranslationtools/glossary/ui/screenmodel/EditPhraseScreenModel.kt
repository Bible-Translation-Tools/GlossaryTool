package org.bibletranslationtools.glossary.ui.screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.Utils.generateUUID
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.GlossaryRepository

sealed class EditPhraseEvent {
    data object Idle : EditPhraseEvent()
    data class SavePhrase(val spelling: String, val description: String) : EditPhraseEvent()
    data object OnPhraseSaved: EditPhraseEvent()
}

class EditPhraseScreenModel(
    private val phrase: Phrase,
    private val resource: Resource,
    private val glossaryRepository: GlossaryRepository
) : ScreenModel {

    private val _event: Channel<EditPhraseEvent> = Channel()
    val event = _event.receiveAsFlow()

    fun onEvent(event: EditPhraseEvent) {
        when (event) {
            is EditPhraseEvent.SavePhrase -> savePhrase(
                event.spelling,
                event.description
            )
            else -> resetChannel()
        }
    }

    private fun savePhrase(spelling: String, description: String) {
        screenModelScope.launch {
            val phrase = phrase.copy(
                spelling = spelling,
                description = description,
                updatedAt = getCurrentTime(),
                id = phrase.id
            )
            val refs = findRefs(phrase)
            if (refs.isNotEmpty()) {
                glossaryRepository.addPhrase(phrase)?.let { phraseId ->
                    glossaryRepository.addRefs(
                        refs.map { ref -> ref.copy(phraseId = phraseId) }
                    )
                }
                _event.send(EditPhraseEvent.OnPhraseSaved)
            } else {
                println("No refs found")
            }
        }
    }

    private fun findRefs(phrase: Phrase): List<Ref> {
        val refs = mutableListOf<Ref>()
        resource.books.forEach { book ->
            book.chapters.forEach { chapter ->
                chapter.verses.forEach { verse ->
                    val regex = Regex(
                        pattern = "\\b${Regex.escape(phrase.phrase)}\\b",
                        option = RegexOption.IGNORE_CASE
                    )
                    val count = regex.findAll(verse.text).count()
                    if (count > 0) {
                        for (i in 1..count) {
                            val ref = Ref(
                                resource = resource.slug,
                                book = book.slug,
                                chapter = chapter.number.toString(),
                                verse = verse.number,
                                phraseId = phrase.id,
                                id = generateUUID()
                            )
                            refs.add(ref)
                        }
                    }
                }
            }
        }
        return refs
    }

    private fun resetChannel() {
        screenModelScope.launch {
            _event.send(EditPhraseEvent.Idle)
        }
    }
}