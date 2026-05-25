package org.bibletranslationtools.glossary.platform

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.bibletranslationtools.glossary.domain.FileSystemProvider
import org.bibletranslationtools.glossary.domain.FileSystemProviderImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

import org.bibletranslationtools.glossary.BaseTest

class ResourceContainerAccessorTest : BaseTest() {

    private lateinit var fileSystemProvider: FileSystemProvider
    private lateinit var accessor: ResourceContainerAccessor

    @BeforeTest
    override fun setUp() {
        super.setUp()
        fileSystemProvider = FileSystemProviderImpl()
        accessor = ResourceContainerAccessor(fileSystemProvider)
    }

    private fun createDummyResourceContainer(zipFile: Path) {
        val tempDir = Path(testRootDir, "dummy_rc")
        SystemFileSystem.createDirectories(tempDir)
        
        val manifestFile = Path(tempDir, "manifest.yaml")
        val manifestContent = """
            dublin_core:
              conformsto: 'rc0.2'
              type: book
              format: text/usfm
              identifier: ulb
              title: Unlocked Literal Bible
              version: '1.0'
              language:
                identifier: en
                title: English
                direction: ltr
              issued: '2024-01-01'
              modified: '2024-01-01'
            checking:
              checking_entity:
                - 'Wycliffe Associates'
              checking_level: '1'
            projects:
              - identifier: tit
                title: Titus
                path: ./01_TIT.usfm
                sort: 1
        """.trimIndent()
        
        // Write manifest
        SystemFileSystem.sink(manifestFile).use { sink ->
            val bytes = manifestContent.encodeToByteArray()
            // Using raw sink since SystemFileSystem sink returns RawSink in some targets
            // Write via buffer or directly
            java.io.FileOutputStream(manifestFile.toString()).use { fos ->
                fos.write(bytes)
            }
        }
        
        val usfmFile = Path(tempDir, "01_TIT.usfm")
        val usfmContent = """
            \id TIT Titus
            \c 1
            \v 1 Paul, a servant of God.
            \v 2 In hope of eternal life.
        """.trimIndent()
        
        // Write USFM
        java.io.FileOutputStream(usfmFile.toString()).use { fos ->
            fos.write(usfmContent.encodeToByteArray())
        }
        
        // Zip the files
        java.io.FileOutputStream(zipFile.toString()).use { fos ->
            java.util.zip.ZipOutputStream(fos).use { zos ->
                for (file in listOf(manifestFile, usfmFile)) {
                    val entryName = file.name
                    zos.putNextEntry(java.util.zip.ZipEntry(entryName))
                    java.io.FileInputStream(file.toString()).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }

    @Test
    fun testReadResourceContainerSuccess() = runTest {
        val zipFile = Path(fileSystemProvider.sources, "en_ulb.zip")
        createDummyResourceContainer(zipFile)

        val resource = accessor.read(zipFile)
        assertNotNull(resource, "Resource container should be parsed successfully")
        assertEquals("en", resource.lang)
        assertEquals("ulb", resource.type)
        assertEquals("1.0", resource.version)
        assertEquals("text/usfm", resource.format)
        assertEquals("en_ulb.zip", resource.filename)
        
        assertEquals(1, resource.books.size)
        val book = resource.books[0]
        assertEquals("tit", book.slug)
        assertEquals("Titus", book.title)

        val chapters = book.chapters
        assertEquals(1, chapters.size)
        val chapter = chapters[0]
        assertEquals(1, chapter.number)

        val verses = chapter.verses
        assertEquals(2, verses.size)
        assertEquals("1", verses[0].number)
        assertEquals("Paul, a servant of God.", verses[0].text)
        assertEquals("2", verses[1].number)
        assertEquals("In hope of eternal life.", verses[1].text)
    }

    @Test
    fun testReadResourceContainerFileNotFound() = runTest {
        val nonExistentPath = Path(testRootDir, "non_existent.zip")
        val resource = accessor.read(nonExistentPath)
        assertNull(resource, "Should return null if file does not exist")
    }
}
