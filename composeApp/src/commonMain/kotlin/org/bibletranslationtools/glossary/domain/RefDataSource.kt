package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.RefEntity

interface RefDataSource {
    suspend fun getByPhrase(phraseId: String): List<RefEntity>
    suspend fun insert(ref: RefEntity): String?
    fun insertInTransaction(ref: RefEntity): String?
    suspend fun delete(id: String): Long
}

class RefDataSourceImpl(db: GlossaryDatabase): RefDataSource {
    private val queries = db.refQueries

    override suspend fun getByPhrase(phraseId: String) = queries.getByPhrase(phraseId)
        .executeAsList()

    override suspend fun insert(ref: RefEntity): String? {
        return insertInTransaction(ref)
    }

    override fun insertInTransaction(ref: RefEntity): String? {
        val result = queries.insert(
            id = ref.id,
            book = ref.book,
            chapter = ref.chapter,
            verse = ref.verse,
            phraseId = ref.phraseId
        )
        if (result.value > 0) {
            return ref.id
        }
        return null
    }

    override suspend fun delete(id: String): Long {
        return queries.delete(id).await()
    }
}