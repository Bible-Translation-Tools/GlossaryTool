package org.bibletranslationtools.glossary.domain.persistence

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.GlossaryEntity

interface GlossaryDataSource {

    suspend fun getAll(): List<GlossaryEntity>
    suspend fun getByCode(code: String): GlossaryEntity?
    suspend fun setVersion(version: Long, id: String): Long
    suspend fun setHasUpdate(hasUpdate: Boolean, id: String): Long
    suspend fun insert(glossary: GlossaryEntity): String?
    suspend fun delete(id: String)
}

class GlossaryDataSourceImpl(db: GlossaryDatabase): GlossaryDataSource {
    private val queries = db.glossaryQueries

    override suspend fun getAll() = queries.getAll().executeAsList()

    override suspend fun getByCode(code: String): GlossaryEntity? {
        return queries.getByCode(code).executeAsOneOrNull()
    }

    override suspend fun setVersion(version: Long, id: String): Long {
        return queries.setVersion(version, id).await()
    }

    override suspend fun setHasUpdate(hasUpdate: Boolean, id: String): Long {
        return queries.setHasUpdate(if (hasUpdate) 1 else 0, id).await()
    }

    override suspend fun insert(glossary: GlossaryEntity): String? {
        val result = queries.insert(
            id = glossary.id,
            code = glossary.code,
            sourceLanguage = glossary.sourceLanguage,
            targetLanguage = glossary.targetLanguage,
            version = glossary.version,
            resourceId = glossary.resourceId,
            updatedAt = glossary.updatedAt
        )
        if (result.await() > 0) {
            return glossary.id
        }
        return null
    }

    override suspend fun delete(id: String) {
        queries.delete(id)
    }
}