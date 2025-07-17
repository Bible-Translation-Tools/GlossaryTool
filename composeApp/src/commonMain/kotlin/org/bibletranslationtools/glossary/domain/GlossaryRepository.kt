package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.toEntity

interface GlossaryRepository {
    suspend fun addPhrase(phrase: Phrase): String?
    suspend fun addRefs(refs: List<Ref>)
}

class GlossaryRepositoryImpl(
    private val glossaryDataSource: GlossaryDataSource,
    private val phraseDataSource: PhraseDataSource,
    private val refDataSource: RefDataSource
) : GlossaryRepository {

    override suspend fun addPhrase(phrase: Phrase): String? {
        val entity = phrase.toEntity()
        return phraseDataSource.insert(entity)
    }

    override suspend fun addRefs(refs: List<Ref>) {
        for (ref in refs.map { it.toEntity() }) {
            refDataSource.insert(ref)
        }
    }
}