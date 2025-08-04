package org.bibletranslationtools.glossary.domain

import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.toEntity
import org.bibletranslationtools.glossary.data.toModel

interface GlossaryRepository {
    suspend fun getGlossary(code: String): Glossary?
    suspend fun getGlossaries(): List<Glossary>
    suspend fun addGlossary(glossary: Glossary): String?
    suspend fun getPhrase(phrase: String, glossaryId: String): Phrase?
    suspend fun getPhrases(glossaryId: String?): List<Phrase>
    suspend fun addPhrase(phrase: Phrase): String?
    suspend fun addRef(ref: Ref)
    suspend fun getRefs(phraseId: String?): List<Ref>
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
}

class GlossaryRepositoryImpl(
    private val glossaryDataSource: GlossaryDataSource,
    private val phraseDataSource: PhraseDataSource,
    private val refDataSource: RefDataSource,
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

    override suspend fun getPhrase(phrase: String, glossaryId: String): Phrase? {
        return phraseDataSource.getByPhrase(phrase, glossaryId)?.toModel()
    }

    override suspend fun getPhrases(glossaryId: String?): List<Phrase> {
        return glossaryId?.let { id ->
            phraseDataSource.getByGlossary(id)
                .map { it.toModel() }
        } ?: emptyList()
    }

    override suspend fun addPhrase(phrase: Phrase): String? {
        val entity = phrase.toEntity()
        return phraseDataSource.insert(entity)
    }

    override suspend fun addRef(ref: Ref) {
        refDataSource.insert(ref.toEntity())
    }

    override suspend fun getRefs(phraseId: String?): List<Ref> {
        return phraseId?.let { id ->
            refDataSource.getByPhrase(id).map { it.toModel() }
        } ?: emptyList()
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
}