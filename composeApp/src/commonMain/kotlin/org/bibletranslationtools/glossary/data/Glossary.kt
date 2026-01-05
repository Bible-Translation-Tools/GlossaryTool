package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.GlossaryEntity
import org.bibletranslationtools.glossary.Utils.generateUUID
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

@Serializable
data class Glossary(
    val code: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val version: Int,
    val hasUpdate: Boolean = false,
    val resourceId: Long? = null,
    @Contextual val createdAt: LocalDateTime = getCurrentTime(),
    @Contextual val updatedAt: LocalDateTime = getCurrentTime(),
    val id: String? = null,
    val remoteId: String? = null
)

fun GlossaryEntity.toModel(
    sourceLanguage: Language,
    targetLanguage: Language
): Glossary {
    return Glossary(
        code = code,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage,
        version = version.toInt(),
        resourceId = resourceId,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        id = id,
        remoteId = remoteId
    )
}

fun Glossary.toEntity(): GlossaryEntity {
    return GlossaryEntity(
        code = code,
        sourceLanguage = sourceLanguage.slug,
        targetLanguage = targetLanguage.slug,
        version = version.toLong(),
        resourceId = resourceId!!,
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        id = id ?: generateUUID(),
        remoteId = remoteId
    )
}

