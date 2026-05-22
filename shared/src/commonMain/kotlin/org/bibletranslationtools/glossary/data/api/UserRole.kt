package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("owner") OWNER,
    @SerialName("admin") ADMIN,
    @SerialName("editor") EDITOR,
    @SerialName("viewer") VIEWER
}
