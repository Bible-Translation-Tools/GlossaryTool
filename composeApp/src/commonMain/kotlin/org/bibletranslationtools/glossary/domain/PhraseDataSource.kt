package org.bibletranslationtools.glossary.domain

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
    fun getAll(glossaryId: Long): List<PhraseEntity>
    fun getByPhrase(phrase: String): PhraseEntity?
    fun getForChapter(resource: String, book: String, chapter: String): List<PhraseEntity>
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

    override fun getAll(glossaryId: Long) = queries.getAll(glossaryId).executeAsList()

    override fun getByPhrase(phrase: String): PhraseEntity? {
        return queries.getByPhrase(phrase).executeAsOneOrNull()
    }

    override fun getForChapter(
        resource: String,
        book: String,
        chapter: String
    ) = queries.getForChapter(resource, book, chapter)
        .executeAsList()

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}