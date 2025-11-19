package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDetails(
    val error: String,
    val details: String? = null
)
