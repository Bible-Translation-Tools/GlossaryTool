package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.PhraseEntity

interface PhraseDataSource {
    suspend fun insert(
        phrase: String,
        spelling: String,
        description: String,
        audio: String,
        glossaryId: Long,
        updatedAt: Long
    )
    fun getAll(glossaryId: Long): Flow<List<PhraseEntity>>
    fun getByPhrase(phrase: String): PhraseEntity?
    fun getForChapter(resource: String, book: String, chapter: String): Flow<List<PhraseEntity>>
    suspend fun delete(id: Long)
}

class PhraseDataSourceImpl(db: GlossaryDatabase): PhraseDataSource {
    private val queries = db.phraseQueries

    override suspend fun insert(
        phrase: String,
        spelling: String,
        description: String,
        audio: String,
        glossaryId: Long,
        updatedAt: Long
    ) {
        queries.insert(phrase, spelling, description, audio, glossaryId, updatedAt)
    }

    override fun getAll(glossaryId: Long) = queries.getAll(glossaryId)
        .asFlow()
        .mapToList(Dispatchers.IO)

    override fun getByPhrase(phrase: String): PhraseEntity? {
        return queries.getByPhrase(phrase).executeAsOneOrNull()
    }

    override fun getForChapter(
        resource: String,
        book: String,
        chapter: String
    ) = queries.getForChapter(resource, book, chapter)
        .asFlow()
        .mapToList(Dispatchers.IO)

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}