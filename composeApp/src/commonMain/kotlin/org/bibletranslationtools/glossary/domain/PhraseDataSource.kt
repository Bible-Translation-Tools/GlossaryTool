package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.PhraseEntity

interface PhraseDataSource {
    suspend fun getByGlossary(glossaryId: String): List<PhraseEntity>
    suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity?
    suspend fun insert(phrase: PhraseEntity): String?
    suspend fun delete(id: String): Long
}

class PhraseDataSourceImpl(db: GlossaryDatabase): PhraseDataSource {
    private val queries = db.phraseQueries

    override suspend fun getByGlossary(glossaryId: String) =
        queries.getByGlossary(glossaryId).executeAsList()

    override suspend fun getByPhrase(phrase: String, glossaryId: String): PhraseEntity? {
        return queries.getByPhrase(phrase, glossaryId).executeAsOneOrNull()
    }

    override suspend fun insert(phrase: PhraseEntity): String? {
        val result = queries.insert(
            id = phrase.id,
            phrase = phrase.phrase,
            spelling = phrase.spelling,
            description = phrase.description,
            audio = phrase.audio,
            glossaryId = phrase.glossaryId,
            updatedAt = phrase.updatedAt
        )
        if (result.await() > 0) {
            return phrase.id
        }
        return null
    }

    override suspend fun delete(id: String): Long {
        return queries.delete(id).await()
    }
}