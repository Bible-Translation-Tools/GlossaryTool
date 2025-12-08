package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryUser(
    val username: String,
    val emoji: String,
    val code: String,
    val published: Boolean = false,
    val role: UserRole = UserRole.VIEWER
)
