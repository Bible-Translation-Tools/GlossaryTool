package org.bibletranslationtools.glossary.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.LanguageEntity

@Serializable
data class Language(
    @SerialName("lc")
    val slug: String = "",
    @SerialName("ln")
    val name: String = "",
    @SerialName("ang")
    val angName: String = name,
    @SerialName("ld")
    val direction: String = "ltr",
    val gw: Boolean = true
)

fun LanguageEntity.toModel(): Language {
    return Language(
        slug = slug,
        name = name,
        angName = angName,
        direction = direction,
        gw = gw == 1L
    )
}

fun Language.toEntity(): LanguageEntity {
    return LanguageEntity(
        slug = slug,
        name = name,
        angName = angName,
        direction = direction,
        gw = if (gw) 1 else 0
    )
}
