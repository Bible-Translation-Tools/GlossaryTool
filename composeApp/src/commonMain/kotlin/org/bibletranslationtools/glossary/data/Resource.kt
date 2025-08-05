package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.ResourceEntity
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

data class Resource(
    val lang: String,
    val type: String,
    val version: String,
    val format: String,
    val url: String,
    val filename: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val books: List<Workbook> = emptyList(),
    val id: Long = 0
) {
    override fun toString(): String {
        return "${lang}_$type"
    }
}

fun ResourceEntity.toModel() = Resource(
    lang = lang,
    type = type,
    version = version,
    format = format,
    url = url,
    filename = filename,
    createdAt = createdAt.toLocalDateTime(),
    modifiedAt = modifiedAt.toLocalDateTime(),
    books = emptyList(),
    id = id
)

fun Resource.toEntity() = ResourceEntity(
    lang = lang,
    type = type,
    version = version,
    format = format,
    url = url,
    filename = filename,
    createdAt = createdAt.toTimestamp(),
    modifiedAt = modifiedAt.toTimestamp(),
    id = id
)
