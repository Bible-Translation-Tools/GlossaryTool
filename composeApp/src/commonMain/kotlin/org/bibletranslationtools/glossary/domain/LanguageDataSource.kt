package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.LanguageEntity

interface LanguageDataSource {
    suspend fun getAll(): List<LanguageEntity>
    suspend fun getBySlug(slug: String): LanguageEntity?
    suspend fun getGatewayLanguages(): List<LanguageEntity>
    suspend fun getTargetLanguages(): List<LanguageEntity>
    suspend fun insert(language: LanguageEntity)
}

class LanguageDataSourceImpl(db: GlossaryDatabase): LanguageDataSource {
    private val queries = db.languageQueries

    override suspend fun getBySlug(slug: String): LanguageEntity? {
        return queries.getBySlug(slug).executeAsOneOrNull()
    }

    override suspend fun getAll(): List<LanguageEntity> {
        return queries.getAll().executeAsList()
    }

    override suspend fun getGatewayLanguages() =
        queries.getGatewayLangs().executeAsList()

    override suspend fun getTargetLanguages() =
        queries.getTargetLangs().executeAsList()

    override suspend fun insert(language: LanguageEntity) {
        queries.insert(
            slug = language.slug,
            name = language.name,
            angName = language.angName,
            direction = language.direction,
            gw = language.gw
        )
    }
}