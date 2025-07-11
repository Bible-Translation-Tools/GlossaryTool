package org.bibletranslationtools.glossary.data

data class Chapter(
    val number: Int,
    private val readVerses: () -> List<Verse>
) {
    val verses: List<Verse> by lazy(readVerses)
}
