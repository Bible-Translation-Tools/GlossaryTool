package org.bibletranslationtools.glossary.data.export

import kotlinx.serialization.Serializable

@Serializable
internal data class GlossaryExport(
    val id: String,
    val code: String,
    val author: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val createdAt: String,
    val updatedAt: String,
    val resource: ResourceExport,
    val phrases: List<PhraseExport>
)
