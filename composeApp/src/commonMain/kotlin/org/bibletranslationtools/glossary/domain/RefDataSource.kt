package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.RefEntity

interface RefDataSource {
    suspend fun insert(
        resource: String,
        book: String,
        chapter: String,
        verse: String,
        phraseId: Long
    )
    fun getForPhrase(phraseId: Long): List<RefEntity>
    fun getForChapter(resource: String, book: String, chapter: String): List<RefEntity>
    suspend fun delete(id: Long)
}

class RefDataSourceImpl(db: GlossaryDatabase): RefDataSource {
    private val queries = db.refQueries

    override suspend fun insert(
        resource: String,
        book: String,
        chapter: String,
        verse: String,
        phraseId: Long
    ) {
        queries.insert(resource, book, chapter, verse, phraseId)
    }

    override fun getForPhrase(phraseId: Long) = queries.getForPhrase(phraseId)
        .executeAsList()

    override fun getForChapter(
        resource: String,
        book: String,
        chapter: String
    ) = queries.getForChapter(resource, book, chapter).executeAsList()

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}