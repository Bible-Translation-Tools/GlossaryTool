package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryUser(
    val user: User,
    val role: UserRole = UserRole.VIEWER
)
