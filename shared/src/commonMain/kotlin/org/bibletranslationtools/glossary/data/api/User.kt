package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val emoji: String,
    val token: String? = null
)
