package org.bibletranslationtools.glossary.domain.usecases

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.files.Path

import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.api.ManifestGlossary
import org.bibletranslationtools.glossary.data.api.ManifestPhrase
import org.bibletranslationtools.glossary.data.api.ManifestResource
import org.bibletranslationtools.glossary.domain.FileSystemProvider
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository

class ExportGlossary(
    private val glossaryRepository: GlossaryRepository,
    private val fileSystemProvider: FileSystemProvider
) {
    suspend operator fun invoke(glossary: Glossary, target: PlatformFile) {
        val resource = glossaryRepository.getResource(glossary.resourceId)
            ?: throw IllegalArgumentException("Resource not found")
        val phrases = glossaryRepository.getPhrases(glossary.id)
        val pendingPhrases = glossaryRepository.getPendingPhrases(glossary.id)

        val export = ManifestGlossary(
            id = glossary.remoteId,
            code = glossary.code,
            sourceLanguage = glossary.sourceLanguage.slug,
            targetLanguage = glossary.targetLanguage.slug,
            version = glossary.version,
            createdAt = glossary.createdAt.toString(),
            updatedAt = glossary.updatedAt.toString(),
            resource = ManifestResource(
                language = resource.lang,
                type = resource.type,
                version = resource.version
            ),
            phrases = phrases.map { phrase ->
                ManifestPhrase(
                    phrase = phrase.phrase,
                    spelling = phrase.spelling,
                    description = phrase.description,
                    audio = phrase.audio,
                    createdAt = phrase.createdAt.toString(),
                    updatedAt = phrase.updatedAt.toString()
                )
            },
            pendingPhrases = pendingPhrases.map { phrase ->
                ManifestPhrase(
                    phrase = phrase.phrase,
                    spelling = phrase.spelling,
                    description = phrase.description,
                    audio = phrase.audio,
                    createdAt = phrase.createdAt.toString(),
                    updatedAt = phrase.updatedAt.toString()
                )
            }
        )

        val json = Utils.JsonLenient.encodeToString(export)
        val tempDir = fileSystemProvider.createTempDir("glossary")

        val glossaryFile = Path(tempDir, "glossary.json")
        fileSystemProvider.writeFile(json, glossaryFile)

        val resourceFile = Path(fileSystemProvider.sources, resource.filename)
        if (!fileSystemProvider.exists(resourceFile)) {
            throw IllegalArgumentException("Resource file not found")
        }
        fileSystemProvider.copyFileToDir(resourceFile, tempDir)

        fileSystemProvider.zipDirectory(tempDir, target)
    }
}
