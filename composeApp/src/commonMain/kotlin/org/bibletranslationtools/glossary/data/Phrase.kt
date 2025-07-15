package org.bibletranslationtools.glossary.data

import org.bibletranslationtools.glossary.PhraseEntity

data class Phrase(
    val phrase: String,
    val spelling: String,
    val description: String,
    val audio: String? = null,
    private val readRefs: () -> List<Ref> = { emptyList() },
) {
    val refs: List<Ref> by lazy(readRefs)
}

fun PhraseEntity.toPhrase(readRefs: () -> List<Ref>): Phrase {
    return Phrase(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio,
        readRefs = readRefs
    )
}
