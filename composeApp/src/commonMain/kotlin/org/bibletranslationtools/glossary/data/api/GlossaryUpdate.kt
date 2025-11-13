package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryUpdate(
    val id: String,
    val code: String,
    val version: Int,
    val createdAt: Long,
    val updatedAt: Long
)
