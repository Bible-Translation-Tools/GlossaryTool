package org.bibletranslationtools.glossary.domain.data

import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.toEntity
import org.bibletranslationtools.glossary.data.toModel
import org.bibletranslationtools.glossary.data.toPendingEntity

interface GlossaryRepository {
    suspend fun getGlossary(code: String): Glossary?
    suspend fun getGlossaries(): List<Glossary>
    suspend fun addGlossary(glossary: Glossary): String?
    suspend fun setGlossaryVersion(version: Long, id: String): Long
    suspend fun setGlossaryHasUpdate(hasUpdate: Boolean, id: String): Long
    suspend fun getPhrase(id: String): Phrase?
    suspend fun getPendingPhrase(id: String): Phrase?
    suspend fun getPhrase(phrase: String, glossaryId: String): Phrase?
    suspend fun getPendingPhrase(phrase: String, glossaryId: String): Phrase?
    suspend fun getPhrases(glossaryId: String?): List<Phrase>
    suspend fun getPendingPhrases(glossaryId: String?): List<Phrase>
    suspend fun addPhrase(phrase: Phrase): String?
    suspend fun addPendingPhrase(phrase: Phrase): String?
    suspend fun getLanguage(slug: String): Language?
    suspend fun getAllLanguages(): List<Language>
    suspend fun getGatewayLanguages(): List<Language>
    suspend fun getTargetLanguages(): List<Language>
    suspend fun addLanguage(language: Language)
    suspend fun getAllResources(): List<Resource>
    suspend fun getResources(lang: String): List<Resource>
    suspend fun getResource(id: Long?): Resource?
    suspend fun getResource(lang: String, type: String): Resource?
    suspend fun addResource(resource: Resource)
    suspend fun deleteResource(id: Long)
    suspend fun batchAddPhrases(phrases: List<Phrase>)
    suspend fun deletePendingPhrase(id: String)
    suspend fun deletePendingByGlossary(glossaryId: String)
}

class GlossaryRepositoryImpl(
    private val glossaryDataSource: GlossaryDataSource,
    private val phraseDataSource: PhraseDataSource,
    private val languageDataSource: LanguageDataSource,
    private val resourceDataSource: ResourceDataSource
) : GlossaryRepository {

    override suspend fun getGlossary(code: String): Glossary? {
        return glossaryDataSource.getByCode(code)?.let { entity ->
            val sourceLanguage = languageDataSource.getBySlug(entity.sourceLanguage)
                ?.toModel()
            val targetLanguage = languageDataSource.getBySlug(entity.targetLanguage)
                ?.toModel()
            if (sourceLanguage != null && targetLanguage != null) {
                entity.toModel(sourceLanguage, targetLanguage)
            } else {
                null
            }
        }
    }

    override suspend fun getGlossaries(): List<Glossary> {
        return glossaryDataSource.getAll().mapNotNull { entity ->
            val sourceLanguage = languageDataSource.getBySlug(entity.sourceLanguage)
                ?.toModel()
            val targetLanguage = languageDataSource.getBySlug(entity.targetLanguage)
                ?.toModel()

            if (sourceLanguage != null && targetLanguage != null) {
                entity.toModel(sourceLanguage, targetLanguage)
            } else {
                null
            }
        }
    }

    override suspend fun addGlossary(glossary: Glossary): String? {
        return glossaryDataSource.insert(glossary.toEntity())
    }

    override suspend fun setGlossaryVersion(version: Long, id: String): Long {
        return glossaryDataSource.setVersion(version, id)
    }

    override suspend fun setGlossaryHasUpdate(hasUpdate: Boolean, id: String): Long {
        return glossaryDataSource.setHasUpdate(hasUpdate, id)
    }

    override suspend fun getPhrase(id: String): Phrase? {
        return phraseDataSource.getById(id)?.toModel()
    }

    override suspend fun getPendingPhrase(id: String): Phrase? {
        return phraseDataSource.getPendingById(id)?.toModel()
    }

    override suspend fun getPhrase(phrase: String, glossaryId: String): Phrase? {
        return phraseDataSource.getByPhrase(phrase, glossaryId)?.toModel()
    }

    override suspend fun getPendingPhrase(phrase: String, glossaryId: String): Phrase? {
        return phraseDataSource.getPendingByPhrase(phrase, glossaryId)?.toModel()
    }

    override suspend fun getPhrases(glossaryId: String?): List<Phrase> {
        return glossaryId?.let { id ->
            phraseDataSource.getByGlossary(id)
                .map { it.toModel() }
        } ?: emptyList()
    }

    override suspend fun getPendingPhrases(glossaryId: String?): List<Phrase> {
        return glossaryId?.let { id ->
            phraseDataSource.getPendingByGlossary(id)
                .map { it.toModel() }
        } ?: emptyList()
    }

    override suspend fun addPhrase(phrase: Phrase): String? {
        val entity = phrase.toEntity()
        return phraseDataSource.insert(entity)
    }

    override suspend fun addPendingPhrase(phrase: Phrase): String? {
        val entity = phrase.toPendingEntity()
        return phraseDataSource.insertPending(entity)
    }

    override suspend fun getLanguage(slug: String): Language? {
        return languageDataSource.getBySlug(slug)?.toModel()
    }

    override suspend fun getAllLanguages(): List<Language> {
        return languageDataSource.getAll().map { it.toModel() }
    }

    override suspend fun getGatewayLanguages(): List<Language> {
        return languageDataSource.getGatewayLanguages().map { it.toModel() }
    }

    override suspend fun getTargetLanguages(): List<Language> {
        return languageDataSource.getTargetLanguages().map { it.toModel() }
    }

    override suspend fun addLanguage(language: Language) {
        languageDataSource.insert(language.toEntity())
    }

    override suspend fun getAllResources(): List<Resource> {
        return resourceDataSource.getAll().map { it.toModel() }
    }

    override suspend fun getResources(lang: String): List<Resource> {
        return resourceDataSource.getByLang(lang).map { it.toModel() }
    }

    override suspend fun getResource(id: Long?): Resource? {
        return id?.let { resourceDataSource.getById(it)?.toModel() }
    }

    override suspend fun getResource(lang: String, type: String): Resource? {
        return resourceDataSource.getByLangType(lang, type)?.toModel()
    }

    override suspend fun addResource(resource: Resource) {
        resourceDataSource.insert(resource.toEntity())
    }

    override suspend fun deleteResource(id: Long) {
        resourceDataSource.delete(id)
    }

    override suspend fun batchAddPhrases(phrases: List<Phrase>) {
        phraseDataSource.transaction {
            phrases.forEach { phrase ->
                phraseDataSource.insertInTransaction(phrase.toEntity())
            }
        }
    }

    override suspend fun deletePendingPhrase(id: String) {
        phraseDataSource.deletePending(id)
    }

    override suspend fun deletePendingByGlossary(glossaryId: String) {
        phraseDataSource.deletePendingByGlossary(glossaryId)
    }
}