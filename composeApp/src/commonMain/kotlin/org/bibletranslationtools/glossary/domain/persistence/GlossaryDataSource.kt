package org.bibletranslationtools.glossary.domain.persistence

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.GlossaryEntity

interface GlossaryDataSource {

    suspend fun getAll(): List<GlossaryEntity>
    suspend fun getById(id: String): GlossaryEntity?
    suspend fun insert(glossary: GlossaryEntity): String
    suspend fun delete(id: String)
}

class GlossaryDataSourceImpl(db: GlossaryDatabase): GlossaryDataSource {
    private val queries = db.glossaryQueries

    override suspend fun getAll() = queries.getAll().executeAsList()

    override suspend fun getById(id: String): GlossaryEntity? {
        return queries.getById(id).executeAsOneOrNull()
    }

    override suspend fun insert(glossary: GlossaryEntity): String {
        return queries.insert(
            id = glossary.id,
            code = glossary.code,
            sourceLanguage = glossary.sourceLanguage,
            targetLanguage = glossary.targetLanguage,
            version = glossary.version,
            resourceId = glossary.resourceId,
            updatedAt = glossary.updatedAt,
            remoteId = glossary.remoteId
        ).executeAsOne()
    }

    override suspend fun delete(id: String) {
        queries.delete(id)
    }
}