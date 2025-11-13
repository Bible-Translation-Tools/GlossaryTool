package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ManifestResource(
    val language: String,
    val type: String,
    val version: String,
    val id: Int? = null
) {
    override fun toString(): String {
        return "${language}_$type"
    }
}