package org.bibletranslationtools.glossary.platform

import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Verse
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.usfmtools.USFMParser
import org.wycliffeassociates.usfmtools.models.markers.CMarker
import org.wycliffeassociates.usfmtools.models.markers.FMarker
import org.wycliffeassociates.usfmtools.models.markers.TextBlock
import org.wycliffeassociates.usfmtools.models.markers.VMarker
import org.wycliffeassociates.usfmtools.models.markers.XMarker
import java.io.File

actual class ResourceContainerAccessor actual constructor(
    private val directoryProvider: DirectoryProvider
) {
    actual fun read(resourceId: String): Resource {
        val resource = Path(
            directoryProvider.sources,
            "$resourceId.zip"
        )
        return read(resource)
    }

    actual fun read(path: Path): Resource {
        if (SystemFileSystem.exists(path)) {
            val resourceContainer = ResourceContainer.load(File(path.toString()))

            val language = resourceContainer.manifest.dublinCore.language.let {
                Language(
                    slug = it.identifier,
                    name = it.title,
                    direction = it.direction
                )
            }

            val books = resourceContainer.manifest.projects.map { project ->
                Workbook(
                    sort = project.sort,
                    slug = project.identifier,
                    title = project.title,
                    language = language
                ) {
                    readChapters(resourceContainer, project.path)
                }
            }

            val core = resourceContainer.manifest.dublinCore
            val issuedDate = LocalDate.parse(core.issued)
            val modifiedDate = LocalDate.parse(core.modified)

            return Resource(
                lang = core.language.identifier,
                type = core.identifier,
                version = core.version,
                createdAt = issuedDate.atTime(0,0),
                modifiedAt = modifiedDate.atTime(0,0),
                books = books
            )
        } else {
            throw IllegalArgumentException("Resource $path is not found.")
        }
    }

    private fun readChapters(
        resourceContainer: ResourceContainer,
        filename: String
    ): List<Chapter> {
        val normalized = Path(filename).name
        resourceContainer.accessor.getReader(normalized).use { reader ->
            val usfm = reader.buffered().readText()
            val parser = USFMParser(arrayListOf("s5"))
            val document = parser.parseFromString(usfm)
            val chapters = document.getChildMarkers(CMarker::class.java)
                .map { chapter ->
                    Chapter(chapter.number) {
                        readVerses(chapter)
                    }
                }
            return chapters
        }
    }

    private fun readVerses(chapter: CMarker): List<Verse> {
        return chapter.getChildMarkers(VMarker::class.java)
            .map { verse ->
                Verse(verse.verseNumber, verse.getText())
            }
    }

    private fun VMarker.getText(): String {
        val ignoredMarkers = listOf(FMarker::class.java, XMarker::class.java)
        val textBlocks = getChildMarkers(TextBlock::class.java, ignoredMarkers)
        val sb = StringBuilder()
        for ((idx, textBlock) in textBlocks.withIndex()) {
            sb.append(textBlock.text.trim())
            if (idx != textBlocks.lastIndex) {
                sb.append(" ")
            }
        }
        return sb.toString()
    }
}