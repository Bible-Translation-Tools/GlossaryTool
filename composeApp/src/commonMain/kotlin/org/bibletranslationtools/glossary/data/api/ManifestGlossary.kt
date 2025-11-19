package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ManifestGlossary(
    val id: String,
    val code: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val version: Int,
    val createdAt: String,
    val updatedAt: String,
    val resource: ManifestResource,
    val phrases: List<ManifestPhrase>
)
