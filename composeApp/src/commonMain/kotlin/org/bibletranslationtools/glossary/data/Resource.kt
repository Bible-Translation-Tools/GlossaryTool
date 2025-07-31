package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.ResourceEntity
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

data class Resource(
    val lang: String,
    val type: String,
    val version: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val books: List<Workbook>,
    val id: Long = 0
)

fun ResourceEntity.toModel() = Resource(
    lang = lang,
    type = type,
    version = version,
    createdAt = createdAt.toLocalDateTime(),
    modifiedAt = modifiedAt.toLocalDateTime(),
    books = emptyList(),
    id = id
)

fun Resource.toEntity() = ResourceEntity(
    lang = lang,
    type = type,
    version = version,
    createdAt = createdAt.toTimestamp(),
    modifiedAt = modifiedAt.toTimestamp(),
    id = id
)
