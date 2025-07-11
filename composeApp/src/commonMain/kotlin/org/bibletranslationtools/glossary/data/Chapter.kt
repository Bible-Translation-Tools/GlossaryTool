package org.bibletranslationtools.glossary.data

data class Chapter(
    val name: String,
    private val readVerses: () -> List<Verse>
) {
    val verses: List<Verse> by lazy(readVerses)
}
