package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    fun getForPhrase(phraseId: Long): Flow<List<RefEntity>>
    fun getForChapter(resource: String, book: String, chapter: String): Flow<List<RefEntity>>
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
        .asFlow()
        .mapToList(Dispatchers.Default)

    override fun getForChapter(
        resource: String,
        book: String,
        chapter: String
    ): Flow<List<RefEntity>> {
        return queries.getForChapter(resource, book, chapter)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}