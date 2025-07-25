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
    val createdAt: LocalDateTime = getCurrentTime(),
    val updatedAt: LocalDateTime = getCurrentTime(),
    val id: String? = null,
    private val getPhrases: () -> List<Phrase> = { emptyList() }
) {
    val phrases: List<Phrase> by lazy(getPhrases)
}

fun GlossaryEntity.toModel(): Glossary {
    return Glossary(
        code = code,
        author = author,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        id = id
    )
}

fun Glossary.toEntity(): GlossaryEntity {
    return GlossaryEntity(
        code = code,
        author = author,
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        id = id ?: generateUUID()
    )
}
