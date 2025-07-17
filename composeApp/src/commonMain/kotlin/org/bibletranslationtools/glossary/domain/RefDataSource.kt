package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.RefEntity

interface RefDataSource {
    suspend fun getByPhrase(phraseId: String): List<RefEntity>
    suspend fun insert(refEntity: RefEntity): String?
    suspend fun delete(id: String): Long
}

class RefDataSourceImpl(db: GlossaryDatabase): RefDataSource {
    private val queries = db.refQueries

    override suspend fun getByPhrase(phraseId: String) = queries.getByPhrase(phraseId)
        .executeAsList()

    override suspend fun insert(refEntity: RefEntity): String? {
        val result = queries.insert(
            id = refEntity.id,
            resource = refEntity.resource,
            book = refEntity.book,
            chapter = refEntity.chapter,
            verse = refEntity.verse,
            phraseId = refEntity.phraseId
        )
        if (result.await() > 0) {
            return refEntity.id
        }
        return null
    }

    override suspend fun delete(id: String): Long {
        return queries.delete(id).await()
    }
}