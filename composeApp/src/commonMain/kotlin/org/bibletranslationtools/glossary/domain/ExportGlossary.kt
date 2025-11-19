package org.bibletranslationtools.glossary.domain

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import org.bibletranslationtools.glossary.Utils.JsonLenient
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.api.ManifestGlossary
import org.bibletranslationtools.glossary.data.api.ManifestPhrase
import org.bibletranslationtools.glossary.data.api.ManifestRef
import org.bibletranslationtools.glossary.data.api.ManifestResource
import org.bibletranslationtools.glossary.platform.zipDirectory

class ExportGlossary(
    private val glossaryRepository: GlossaryRepository,
    private val directoryProvider: DirectoryProvider
) {
    suspend operator fun invoke(glossary: Glossary, target: PlatformFile) {
        val resource = glossaryRepository.getResource(glossary.resourceId)
            ?: throw IllegalArgumentException("Resource not found")
        val phrases = glossaryRepository.getPhrases(glossary.id)

        val export = ManifestGlossary(
            id = glossary.id!!,
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
                val refs = glossaryRepository.getRefs(phrase.id)
                ManifestPhrase(
                    id = phrase.id!!,
                    phrase = phrase.phrase,
                    spelling = phrase.spelling,
                    description = phrase.description,
                    audio = phrase.audio,
                    createdAt = phrase.createdAt.toString(),
                    updatedAt = phrase.updatedAt.toString(),
                    refs = refs.map { ref ->
                        ManifestRef(
                            id = ref.id!!,
                            book = ref.book,
                            chapter = ref.chapter,
                            verse = ref.verse
                        )
                    }
                )
            }
        )

        val json = JsonLenient.encodeToString(export)
        val tempDir = directoryProvider.createTempDir("glossary")

        val glossaryFile = Path(tempDir, "glossary.json")
        SystemFileSystem.sink(glossaryFile).buffered().use { sink ->
            sink.writeString(json)
        }

        val resourceFile = Path(directoryProvider.sources, resource.filename)
        if (!SystemFileSystem.exists(resourceFile)) {
            throw IllegalArgumentException("Resource file not found")
        }
        directoryProvider.copyFileToDir(resourceFile, tempDir)

        zipDirectory(tempDir, target)
    }
}