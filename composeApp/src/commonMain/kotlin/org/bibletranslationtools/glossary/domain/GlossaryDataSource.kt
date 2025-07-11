package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.GlossaryEntity

interface GlossaryDataSource {
    suspend fun insert(code: String, author: String, updatedAt: Long)
    fun getAll(): Flow<List<GlossaryEntity>>
    suspend fun getByCode(code: String): GlossaryEntity?
    suspend fun delete(id: Long)
}

class GlossaryDataSourceImpl(db: GlossaryDatabase): GlossaryDataSource {
    private val queries = db.glossaryQueries

    override suspend fun insert(code: String, author: String, updatedAt: Long) {
        queries.insert(code, author, updatedAt)
    }

    override fun getAll() = queries.getAll().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getByCode(code: String): GlossaryEntity? {
        return queries.getByCode(code).executeAsOneOrNull()
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}