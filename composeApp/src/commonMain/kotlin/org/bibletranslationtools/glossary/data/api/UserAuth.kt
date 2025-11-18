package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class UserAuth(
    val username: String,
    val password: String
)
