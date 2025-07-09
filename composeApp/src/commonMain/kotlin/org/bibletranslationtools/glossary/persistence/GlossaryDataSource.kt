package org.bibletranslationtools.glossary.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.GlossaryEntity

interface GlossaryDataSource {
    suspend fun insert(code: String, author: String, updatedAt: Long)
    fun getAll(): Flow<List<GlossaryEntity>>
    fun getByCode(code: String): GlossaryEntity?
    suspend fun delete(id: Long)
}

class GlossaryDataSourceImpl(db: GlossaryDatabase): GlossaryDataSource {
    private val queries = db.glossaryQueries

    override suspend fun insert(code: String, author: String, updatedAt: Long) {
        println("GlossaryDataSourceImpl: Inserting record: $code")
        try {
            queries.insert(code, author, updatedAt)
            println("GlossaryDataSourceImpl: Inserted record: $code")
        } catch (e: Exception) {
            println("GlossaryDataSourceImpl: Error inserting record: $code - ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getAll() = queries.getAll().asFlow().mapToList(Dispatchers.Default).also {
        println("GlossaryDataSourceImpl: getAll() called.")
    }

    override fun getByCode(code: String): GlossaryEntity? {
        return queries.getByCode(code).executeAsOneOrNull()
    }

    override suspend fun delete(id: Long) {
        queries.delete(id)
    }
}