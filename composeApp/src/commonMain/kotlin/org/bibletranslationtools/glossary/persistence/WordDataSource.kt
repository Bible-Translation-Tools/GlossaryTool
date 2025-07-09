package org.bibletranslationtools.glossary.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.WordEntity

interface WordDataSource {
    suspend fun insert(
        word: String,
        reference: String,
        description: String,
        glossaryId: Long,
        updatedAt: Long
    )
    fun getAll(glossaryId: Long): Flow<List<WordEntity>>
    fun getByWord(word: String): WordEntity?
    suspend fun delete(id: Long)
}

class WordDataSourceImpl(db: GlossaryDatabase): WordDataSource {
    private val queries = db.wordQueries

    override suspend fun insert(word: String, reference: String, description: String, glossaryId: Long, updatedAt: Long) {
        queries.insert(word, reference, description, glossaryId, updatedAt)
    }

    override fun getAll(glossaryId: Long) = queries.getAll(glossaryId).asFlow().mapToList(Dispatchers.Default)

    override fun getByWord(word: String): WordEntity? {
        return queries.getByWord(word).executeAsOneOrNull()
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}