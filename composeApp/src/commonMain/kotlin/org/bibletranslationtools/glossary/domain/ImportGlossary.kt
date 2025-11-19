package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.bibletranslationtools.glossary.Utils.JsonLenient
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.api.ManifestGlossary
import org.bibletranslationtools.glossary.data.api.ManifestPhrase
import org.bibletranslationtools.glossary.data.api.ManifestRef
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.extractZip
import org.bibletranslationtools.glossary.toLocalDateTime

class ImportGlossary(
    private val glossaryRepository: GlossaryRepository,
    private val directoryProvider: DirectoryProvider,
    private val resourceContainerAccessor: ResourceContainerAccessor
) {
    data class Result(
        val glossary: Glossary,
        val resource: Resource
    )

    suspend operator fun invoke(file: PlatformFile): Result {

        val tempDir = directoryProvider.createTempDir("glossary")
        extractZip(file, tempDir)

        val glossaryFile = Path(tempDir, "glossary.json")

        if (!SystemFileSystem.exists(glossaryFile)) {
            throw IllegalArgumentException("glossary.json not found in zip file")
        }

        val json = SystemFileSystem.source(glossaryFile).buffered().use { source ->
            source.readString()
        }

        val glossaryDict: ManifestGlossary = JsonLenient.decodeFromString(json)

        val resourceId = glossaryDict.resource.toString()
        val resourceFile = Path(tempDir, "$resourceId.zip")

        if (!SystemFileSystem.exists(resourceFile)) {
            throw IllegalArgumentException("$resourceId.zip not found in zip file")
        }

        val resource = resourceContainerAccessor.read(resourceFile)?.let { resource ->
            try {
                glossaryRepository.addResource(resource)
            } catch (_: Exception) {}
            val dbResource = glossaryRepository.getResource(
                glossaryDict.resource.language,
                glossaryDict.resource.type
            )
            resource.copy(id = dbResource!!.id, url = dbResource.url)
        } ?: throw IllegalArgumentException("Resource is corrupted")

        directoryProvider.saveSource(resourceFile, "$resourceId.zip")

        val glossary = mapGlossary(glossaryDict, resource)
        val glossaryId = glossaryRepository.addGlossary(glossary)

        val phrasesToInsert = mutableListOf<Phrase>()
        val refsToInsert = mutableListOf<Ref>()

        glossaryDict.phrases.forEach { phrase ->
            val dbPhrase = mapPhrase(phrase, glossaryId!!)
            phrasesToInsert.add(dbPhrase)

            phrase.refs.forEach { ref ->
                val dbRef = mapRef(ref, dbPhrase.id!!)
                refsToInsert.add(dbRef)
            }
        }

        glossaryRepository.batchAddPhrasesAndRefs(phrasesToInsert, refsToInsert)

        return Result(
            glossary = glossary,
            resource = resource
        )
    }

    private suspend fun mapGlossary(
        glossary: ManifestGlossary,
        resource: Resource
    ): Glossary {
        val sourceLanguage = glossaryRepository.getLanguage(glossary.sourceLanguage)
        val targetLanguage = glossaryRepository.getLanguage(glossary.targetLanguage)

        if (sourceLanguage == null) {
            throw IllegalArgumentException("Source language not found in database")
        }

        if (targetLanguage == null) {
            throw IllegalArgumentException("Target language not found in database")
        }

        return Glossary(
            id = glossary.id,
            code = glossary.code,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            version = glossary.version,
            hasUpdate = false,
            resourceId = resource.id,
            createdAt = glossary.createdAt.toLocalDateTime(),
            updatedAt = glossary.updatedAt.toLocalDateTime(),
        )
    }

    private fun mapPhrase(phrase: ManifestPhrase, glossaryId: String): Phrase {
        return Phrase(
            id = phrase.id,
            phrase = phrase.phrase,
            spelling = phrase.spelling,
            description = phrase.description,
            audio = phrase.audio,
            createdAt = phrase.createdAt.toLocalDateTime(),
            updatedAt = phrase.updatedAt.toLocalDateTime(),
            glossaryId = glossaryId
        )
    }

    private fun mapRef(ref: ManifestRef, phraseId: String): Ref {
        return Ref(
            id = ref.id,
            book = ref.book,
            chapter = ref.chapter,
            verse = ref.verse,
            phraseId = phraseId
        )
    }
}