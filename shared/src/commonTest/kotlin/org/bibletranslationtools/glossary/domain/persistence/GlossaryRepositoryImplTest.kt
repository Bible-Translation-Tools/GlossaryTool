package org.bibletranslationtools.glossary.domain.persistence

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.BaseTest
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.platform.createSqlDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GlossaryRepositoryImplTest : BaseTest() {

    private lateinit var driver: SqlDriver
    private lateinit var db: GlossaryDatabase
    private lateinit var repository: GlossaryRepository

    @BeforeTest
    override fun setUp() {
        super.setUp()
        driver = createSqlDriver()
        db = GlossaryDatabase(driver)

        val glossaryDataSource = GlossaryDataSourceImpl(db)
        val phraseDataSource = PhraseDataSourceImpl(db)
        val languageDataSource = LanguageDataSourceImpl(db)
        val resourceDataSource = ResourceDataSourceImpl(db)

        repository = GlossaryRepositoryImpl(
            glossaryDataSource = glossaryDataSource,
            phraseDataSource = phraseDataSource,
            languageDataSource = languageDataSource,
            resourceDataSource = resourceDataSource
        )
    }

    @AfterTest
    override fun tearDown() {
        driver.close()
        super.tearDown()
    }

    private suspend fun addDummyResource(lang: String = "en", type: String = "ulb") {
        // Satisfy foreign key to LanguageEntity(slug)
        val language = Language(lang, "LanguageName", "ltr")
        repository.addLanguage(language)

        val resource = Resource(
            lang = lang,
            type = type,
            version = "1.0",
            format = "usfm",
            url = "",
            filename = "${lang}_${type}.zip",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0),
            modifiedAt = LocalDateTime(2024, 1, 1, 0, 0)
        )
        repository.addResource(resource)
    }

    private suspend fun addDummyGlossary(
        id: String,
        sourceLang: Language = Language("en", "English", "ltr"),
        targetLang: Language = Language("es", "Spanish", "ltr")
    ): Long {
        repository.addLanguage(sourceLang)
        repository.addLanguage(targetLang)
        addDummyResource(lang = sourceLang.slug, type = "ulb")

        // Retrieve generated resource ID from DB
        val resourceId = repository.getAllResources().first { it.lang == sourceLang.slug }.id

        val glossary = Glossary(
            code = "glossary_code_$id",
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            version = 1,
            resourceId = resourceId,
            id = id
        )
        repository.addGlossary(glossary)
        return resourceId
    }

    @Test
    fun testAddAndGetLanguage() = runTest {
        val language = Language("en", "English", "ltr")
        repository.addLanguage(language)

        val fetched = repository.getLanguage("en")
        assertNotNull(fetched)
        assertEquals(language.slug, fetched.slug)
        assertEquals(language.name, fetched.name)
        assertEquals(language.direction, fetched.direction)

        val all = repository.getAllLanguages()
        assertEquals(1, all.size)
        assertEquals(language.slug, all[0].slug)
    }

    @Test
    fun testAddAndGetGlossary() = runTest {
        val resourceId = addDummyGlossary(id = "g_id")

        val fetched = repository.getGlossary("g_id")
        assertNotNull(fetched)
        assertEquals("g_id", fetched.id)
        assertEquals(resourceId, fetched.resourceId)

        val all = repository.getGlossaries()
        assertEquals(1, all.size)
        assertEquals("g_id", all[0].id)
    }

    @Test
    fun testAddAndGetPhrases() = runTest {
        addDummyGlossary(id = "g_id")

        val phrase1 = Phrase("God", spelling = "God", description = "Creator", glossaryId = "g_id", id = "p1")
        repository.addPhrase(phrase1)

        val fetched = repository.getPhrase("p1")
        assertNotNull(fetched)
        assertEquals(phrase1.phrase, fetched.phrase)
        assertEquals(phrase1.glossaryId, fetched.glossaryId)

        val fetchedByText = repository.getPhrase("God", "g_id")
        assertNotNull(fetchedByText)
        assertEquals("p1", fetchedByText.id)

        // Non-existent glossary id should return empty list
        val phrasesEmpty = repository.getPhrases("invalid_g_id")
        assertTrue(phrasesEmpty.isEmpty())

        val phrases = repository.getPhrases("g_id")
        assertEquals(1, phrases.size)
        assertEquals("p1", phrases[0].id)
    }

    @Test
    fun testAddAndGetPendingPhrases() = runTest {
        addDummyGlossary(id = "g_id")

        val phrase = Phrase("Heaven", spelling = "Heaven", description = "Place", glossaryId = "g_id", id = "p_pending")
        repository.addPendingPhrase(phrase)

        val fetchedPending = repository.getPendingPhrase("p_pending")
        assertNotNull(fetchedPending)
        assertEquals("Heaven", fetchedPending.phrase)
        assertTrue(fetchedPending.pending)

        val fetchedPendingByText = repository.getPendingPhrase("Heaven", "g_id")
        assertNotNull(fetchedPendingByText)
        assertEquals("p_pending", fetchedPendingByText.id)

        val pendingPhrases = repository.getPendingPhrases("g_id")
        assertEquals(1, pendingPhrases.size)
        assertEquals("p_pending", pendingPhrases[0].id)

        // Test deletePendingPhrase
        repository.deletePendingPhrase("p_pending")
        assertNull(repository.getPendingPhrase("p_pending"))
    }

    @Test
    fun testAddAndGetResource() = runTest {
        val lang = Language("en", "English", "ltr")
        repository.addLanguage(lang)

        val resource = Resource(
            lang = "en",
            type = "ulb",
            version = "1.0",
            format = "usfm",
            url = "http://example.com/en_ulb.zip",
            filename = "en_ulb.zip",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0),
            modifiedAt = LocalDateTime(2024, 1, 1, 0, 0)
        )

        repository.addResource(resource)

        val all = repository.getAllResources()
        assertEquals(1, all.size)
        val generatedId = all[0].id

        val fetched = repository.getResource(generatedId)
        assertNotNull(fetched)
        assertEquals(resource.lang, fetched.lang)
        assertEquals(resource.type, fetched.type)

        val fetchedByLangType = repository.getResource("en", "ulb")
        assertNotNull(fetchedByLangType)
        assertEquals(generatedId, fetchedByLangType.id)

        repository.deleteResource(generatedId)
        assertNull(repository.getResource(generatedId))
    }

    @Test
    fun testBatchOperations() = runTest {
        addDummyGlossary(id = "g_id")

        val phrase1 = Phrase("Word1", glossaryId = "g_id", id = "p1")
        val phrase2 = Phrase("Word2", glossaryId = "g_id", id = "p2")

        // Batch add standard phrases
        repository.batchAddPhrases(listOf(phrase1, phrase2))
        val phrases = repository.getPhrases("g_id")
        assertEquals(2, phrases.size)

        // Batch add pending phrases
        val pending1 = Phrase("Word3", glossaryId = "g_id", id = "p3")
        val pending2 = Phrase("Word4", glossaryId = "g_id", id = "p4")
        repository.batchAddPendingPhrases(listOf(pending1, pending2))
        val pendings = repository.getPendingPhrases("g_id")
        assertEquals(2, pendings.size)

        // Delete pending phrases by glossary
        repository.deletePendingByGlossary("g_id")
        val pendingsAfterDelete = repository.getPendingPhrases("g_id")
        assertTrue(pendingsAfterDelete.isEmpty())
    }
}
