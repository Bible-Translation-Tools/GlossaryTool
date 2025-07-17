package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.PhraseEntity
import org.bibletranslationtools.glossary.Utils.generateUUID
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

data class Phrase(
    val phrase: String,
    val spelling: String = "",
    val description: String = "",
    val audio: String? = null,
    val createdAt: LocalDateTime = getCurrentTime(),
    val updatedAt: LocalDateTime = getCurrentTime(),
    val glossaryId: String? = null,
    val id: String? = null,
    private val readRefs: () -> List<Ref> = { emptyList() }
) {
    val refs: List<Ref> by lazy(readRefs)
}

fun PhraseEntity.toData(readRefs: () -> List<Ref>): Phrase {
    return Phrase(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        readRefs = readRefs,
        glossaryId = glossaryId,
        id = this.id
    )
}

fun Phrase.toEntity(): PhraseEntity {
    return PhraseEntity(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio ?: "",
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        glossaryId = glossaryId!!,
        id = id ?: generateUUID()
    )
}
