package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.PhraseEntity

interface PhraseDataSource {
    suspend fun getByGlossary(glossaryId: String): List<PhraseEntity>
    suspend fun getById(id: String): PhraseEntity?
    suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity?
    suspend fun insert(phrase: PhraseEntity): String?
    fun insertInTransaction(phrase: PhraseEntity): String?
    suspend fun delete(id: String): Long
    fun transaction(body: () -> Unit)
}

class PhraseDataSourceImpl(db: GlossaryDatabase): PhraseDataSource {
    private val queries = db.phraseQueries

    override suspend fun getByGlossary(glossaryId: String) =
        queries.getByGlossary(glossaryId).executeAsList()

    override suspend fun getById(id: String): PhraseEntity? {
        return queries.getById(id).executeAsOneOrNull()
    }

    override suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity? {
        return queries.getByPhrase(phrase, glossaryId).executeAsOneOrNull()
    }

    override suspend fun insert(phrase: PhraseEntity): String? {
        return insertInTransaction(phrase)
    }

    override fun insertInTransaction(phrase: PhraseEntity): String? {
        val result = queries.insert(
            id = phrase.id,
            phrase = phrase.phrase,
            spelling = phrase.spelling,
            description = phrase.description,
            audio = phrase.audio,
            glossaryId = phrase.glossaryId,
            updatedAt = phrase.updatedAt
        )
        if (result.value > 0) {
            return phrase.id
        }
        return null
    }

    override suspend fun delete(id: String): Long {
        return queries.delete(id).await()
    }

    override fun transaction(body: () -> Unit) {
        queries.transaction {
            body()
        }
    }
}