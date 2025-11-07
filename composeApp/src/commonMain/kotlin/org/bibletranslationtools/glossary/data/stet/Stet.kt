package org.bibletranslationtools.glossary.data.stet

import kotlinx.serialization.Serializable

@Serializable
data class Stet(
    val word: String,
    val strongs: List<String>,
    val description: String,
    val references: List<String>,
    val alternatives: List<String>
)