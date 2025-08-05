package org.bibletranslationtools.glossary.data.export

import kotlinx.serialization.Serializable

@Serializable
internal data class RefExport(
    val id: String,
    val book: String,
    val chapter: String,
    val verse: String
)