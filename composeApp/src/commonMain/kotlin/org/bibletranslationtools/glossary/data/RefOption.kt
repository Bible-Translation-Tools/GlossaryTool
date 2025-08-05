package org.bibletranslationtools.glossary.data

import kotlinx.serialization.Serializable

@Serializable
data class RefOption(
    val book: String,
    val chapter: Int,
    val verse: String? = null,
)

fun Ref.toOption(): RefOption {
    return RefOption(
        book = book,
        chapter = chapter.toInt(),
        verse = verse
    )
}
