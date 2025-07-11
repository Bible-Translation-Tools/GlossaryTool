package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.SettingsEntity

interface SettingsDataSource {
    suspend fun insert(name: String, value: String)
    fun getAll(): Flow<List<SettingsEntity>>
    suspend fun getByName(name: String): SettingsEntity?
    suspend fun delete(name: String)
}

class SettingsDataSourceImpl(db: GlossaryDatabase): SettingsDataSource {
    private val queries = db.settingsQueries

    override suspend fun insert(name: String, value: String) {
        queries.insert(name, value)
    }

    override fun getAll() = queries.getAll().asFlow().mapToList(Dispatchers.Default)

    override suspend fun getByName(name: String): SettingsEntity? {
        return queries.getByName(name).executeAsOneOrNull()
    }

    override suspend fun delete(name: String) {
        queries.delete(name)
    }
}