package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryUser(
    val username: String,
    val emoji: String,
    val role: UserRole = UserRole.VIEWER,
    val code: String
)
