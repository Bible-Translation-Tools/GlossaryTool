package org.bibletranslationtools.glossary.data.export

import kotlinx.serialization.Serializable

@Serializable
internal data class PhraseExport(
    val id: String,
    val phrase: String,
    val spelling: String,
    val description: String,
    val audio: String?,
    val createdAt: String,
    val updatedAt: String,
    val refs: List<RefExport>
)