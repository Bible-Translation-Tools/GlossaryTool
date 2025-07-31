package org.bibletranslationtools.glossary.data

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
