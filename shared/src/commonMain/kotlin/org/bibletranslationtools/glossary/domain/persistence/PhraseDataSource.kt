package org.bibletranslationtools.glossary.domain.persistence

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.PendingPhraseEntity
import org.bibletranslationtools.glossary.PhraseEntity

interface PhraseDataSource {
    suspend fun getByGlossary(glossaryId: String): List<PhraseEntity>
    suspend fun getPendingByGlossary(glossaryId: String): List<PendingPhraseEntity>
    suspend fun getById(id: String): PhraseEntity?
    suspend fun getPendingById(id: String): PendingPhraseEntity?
    suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity?
    suspend fun getPendingByPhrase(phrase: String, glossaryId: String): PendingPhraseEntity?
    suspend fun insert(phrase: PhraseEntity): String
    suspend fun insertPending(phrase: PendingPhraseEntity): String
    fun insertInTransaction(phrase: PhraseEntity): String
    fun insertPendingInTransaction(phrase: PendingPhraseEntity): String
    suspend fun delete(id: String): Long
    suspend fun deletePending(id: String): Long
    suspend fun deletePendingByGlossary(glossaryId: String): Long
    fun transaction(body: () -> Unit)
}

class PhraseDataSourceImpl(db: GlossaryDatabase): PhraseDataSource {
    private val queries = db.phraseQueries
    private val pendingQueries = db.pendingPhraseQueries

    override suspend fun getByGlossary(glossaryId: String) =
        queries.getByGlossary(glossaryId).executeAsList()

    override suspend fun getPendingByGlossary(glossaryId: String) =
        pendingQueries.getByGlossary(glossaryId).executeAsList()

    override suspend fun getById(id: String): PhraseEntity? {
        return queries.getById(id).executeAsOneOrNull()
    }

    override suspend fun getPendingById(id: String): PendingPhraseEntity? {
        return pendingQueries.getById(id).executeAsOneOrNull()
    }

    override suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity? {
        return queries.getByPhrase(phrase, glossaryId).executeAsOneOrNull()
    }

    override suspend fun getPendingByPhrase(
        phrase: String,
        glossaryId: String
    ): PendingPhraseEntity? {
        return pendingQueries.getByPhrase(phrase, glossaryId).executeAsOneOrNull()
    }

    override suspend fun insert(phrase: PhraseEntity): String {
        return insertInTransaction(phrase)
    }

    override suspend fun insertPending(phrase: PendingPhraseEntity): String {
        return insertPendingInTransaction(phrase)
    }

    override fun insertInTransaction(phrase: PhraseEntity): String {
        return queries.insert(
            id = phrase.id,
            phrase = phrase.phrase,
            spelling = phrase.spelling,
            description = phrase.description,
            audio = phrase.audio,
            glossaryId = phrase.glossaryId,
            updatedAt = phrase.updatedAt
        ).executeAsOne()
    }

    override fun insertPendingInTransaction(phrase: PendingPhraseEntity): String {
        return pendingQueries.insert(
            id = phrase.id,
            phrase = phrase.phrase,
            spelling = phrase.spelling,
            description = phrase.description,
            audio = phrase.audio,
            glossaryId = phrase.glossaryId,
            updatedAt = phrase.updatedAt
        ).executeAsOne()
    }

    override suspend fun delete(id: String): Long {
        return queries.delete(id).await()
    }

    override suspend fun deletePending(id: String): Long {
        return pendingQueries.delete(id).await()
    }

    override suspend fun deletePendingByGlossary(glossaryId: String): Long {
        return pendingQueries.deleteByGlosary(glossaryId).await()
    }

    override fun transaction(body: () -> Unit) {
        queries.transaction {
            body()
        }
    }
}