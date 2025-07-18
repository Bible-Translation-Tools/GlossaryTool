package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.toModel
import org.bibletranslationtools.glossary.data.toEntity

interface GlossaryRepository {
    suspend fun getGlossary(code: String): Glossary?
    suspend fun addGlossary(glossary: Glossary): String?
    suspend fun getPhrase(phrase: String, glossaryId: String): Phrase?
    suspend fun getPhrases(glossaryId: String): List<Phrase>
    suspend fun addPhrase(phrase: Phrase): String?
    suspend fun addRefs(refs: List<Ref>)
    suspend fun getRefs(phraseId: String): List<Ref>
}

class GlossaryRepositoryImpl(
    private val glossaryDataSource: GlossaryDataSource,
    private val phraseDataSource: PhraseDataSource,
    private val refDataSource: RefDataSource
) : GlossaryRepository {

    override suspend fun getGlossary(code: String): Glossary? {
        return glossaryDataSource.getByCode(code)?.toModel()
    }

    override suspend fun addGlossary(glossary: Glossary): String? {
        return glossaryDataSource.insert(glossary.toEntity())
    }

    override suspend fun getPhrase(phrase: String, glossaryId: String): Phrase? {
        return phraseDataSource.getByPhrase(phrase, glossaryId)?.toModel()
    }

    override suspend fun getPhrases(glossaryId: String): List<Phrase> {
        return phraseDataSource.getByGlossary(glossaryId)
            .map { it.toModel() }
    }

    override suspend fun addPhrase(phrase: Phrase): String? {
        val entity = phrase.toEntity()
        return phraseDataSource.insert(entity)
    }

    override suspend fun addRefs(refs: List<Ref>) {
        for (ref in refs.map { it.toEntity() }) {
            refDataSource.insert(ref)
        }
    }

    override suspend fun getRefs(phraseId: String): List<Ref> {
        return refDataSource.getByPhrase(phraseId).map { it.toModel() }
    }
}