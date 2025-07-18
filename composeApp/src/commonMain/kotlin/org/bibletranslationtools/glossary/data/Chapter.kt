package org.bibletranslationtools.glossary.data

data class Chapter(
    val number: Int,
    private val getVerses: () -> List<Verse>
) {
    val verses: List<Verse> by lazy(getVerses)
}
