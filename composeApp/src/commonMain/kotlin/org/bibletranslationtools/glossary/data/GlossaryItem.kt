package org.bibletranslationtools.glossary.data

data class GlossaryItem(
    val phrase: String,
    val spelling: String,
    val description: String,
    val refs: List<Ref>,
    val audio: String? = null
)
