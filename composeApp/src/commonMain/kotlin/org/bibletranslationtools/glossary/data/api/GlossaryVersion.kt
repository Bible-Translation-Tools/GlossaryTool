package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryVersion(
    val id: String,
    val version: Int
)
