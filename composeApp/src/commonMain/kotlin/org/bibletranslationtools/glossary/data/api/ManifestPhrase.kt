package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ManifestPhrase(
    val phrase: String,
    val spelling: String,
    val description: String,
    val audio: String?,
    val createdAt: String,
    val updatedAt: String
)