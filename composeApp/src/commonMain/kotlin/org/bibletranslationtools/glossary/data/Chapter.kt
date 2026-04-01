package org.bibletranslationtools.glossary.data

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val number: Int,
    private val getVerses: () -> List<Verse>
) {
    val verses: List<Verse> by lazy(getVerses)
}
