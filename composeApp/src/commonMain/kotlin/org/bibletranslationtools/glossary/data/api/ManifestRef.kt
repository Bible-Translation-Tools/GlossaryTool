package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ManifestRef(
    val id: String,
    val book: String,
    val chapter: String,
    val verse: String
)