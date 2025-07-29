package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.GlossaryEntity
import org.bibletranslationtools.glossary.Utils.generateUUID
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

data class Glossary(
    val code: String,
    val author: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val createdAt: LocalDateTime = getCurrentTime(),
    val updatedAt: LocalDateTime = getCurrentTime(),
    val id: String? = null
)

fun GlossaryEntity.toModel(
    sourceLanguage: Language,
    targetLanguage: Language
): Glossary {
    return Glossary(
        code = code,
        author = author,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        id = id
    )
}

fun Glossary.toEntity(): GlossaryEntity {
    return GlossaryEntity(
        code = code,
        author = author,
        sourceLanguage = sourceLanguage.slug,
        targetLanguage = targetLanguage.slug,
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        id = id ?: generateUUID()
    )
}
