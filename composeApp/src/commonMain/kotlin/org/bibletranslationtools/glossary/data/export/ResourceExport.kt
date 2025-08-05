package org.bibletranslationtools.glossary.data.export

import kotlinx.serialization.Serializable

@Serializable
internal data class ResourceExport(
    val language: String,
    val type: String,
    val version: String
) {
    override fun toString(): String {
        return "${language}_$type"
    }
}