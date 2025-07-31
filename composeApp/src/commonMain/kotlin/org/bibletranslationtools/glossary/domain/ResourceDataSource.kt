package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.ResourceEntity

interface ResourceDataSource {
    suspend fun getAll(): List<ResourceEntity>
    suspend fun getByLangType(langSlug: String, type: String): ResourceEntity?
    suspend fun insert(resource: ResourceEntity)
    suspend fun delete(id: Long)
}

class ResourceDataSourceImpl(db: GlossaryDatabase): ResourceDataSource {
    private val queries = db.resourceQueries

    override suspend fun getAll() = queries.getAll().executeAsList()

    override suspend fun getByLangType(langSlug: String, type: String): ResourceEntity? {
        return queries.getByLangType(langSlug, type).executeAsOneOrNull()
    }

    override suspend fun insert(resource: ResourceEntity) {
        queries.insert(
            lang = resource.lang,
            type = resource.type,
            version = resource.version,
            createdAt = resource.createdAt,
            modifiedAt = resource.modifiedAt
        )
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}