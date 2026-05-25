package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.db.SqlDriver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.BaseTest
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.domain.persistence.LanguageDataSourceImpl
import org.bibletranslationtools.glossary.domain.persistence.ResourceDataSourceImpl
import org.bibletranslationtools.glossary.domain.persistence.SettingsDataSourceImpl
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitAppTest : BaseTest() {

    private lateinit var driver: SqlDriver
    private lateinit var db: GlossaryDatabase

    private lateinit var settings: SettingsDataSourceImpl
    private lateinit var languageDataSource: LanguageDataSourceImpl
    private lateinit var resourceDataSource: ResourceDataSourceImpl

    private val fileSystemProvider: FileSystemProvider = mockk(relaxed = true)
    private val resourceContainerAccessor: ResourceContainerAccessor = mockk(relaxed = true)
    private val catalogApi: CatalogApi = mockk(relaxed = true)

    private lateinit var initApp: InitApp

    @BeforeTest
    override fun setUp() {
        super.setUp()
        driver = createSqlDriver()
        db = GlossaryDatabase(driver)

        settings = SettingsDataSourceImpl(db)
        languageDataSource = LanguageDataSourceImpl(db)
        resourceDataSource = ResourceDataSourceImpl(db)

        initApp = InitApp(
            settings = settings,
            languageDataSource = languageDataSource,
            resourceDataSource = resourceDataSource,
            fileSystemProvider = fileSystemProvider,
            resourceContainerAccessor = resourceContainerAccessor,
            catalogApi = catalogApi
        )
    }

    @AfterTest
    override fun tearDown() {
        driver.close()
        super.tearDown()
    }

    @Test
    fun testInitAppNotInitialized() = runTest {
        // Verify init flag is false initially
        val initialInit = settings.getByName(DbSettings.INIT)?.value_?.toBoolean() ?: false
        assertFalse(initialInit)

        // Setup mock catalog returning English ULB resource
        val catalogFormat = CatalogFormat(format = "usfm", url = "http://example.com/en_ulb.zip")
        val catalogResource = CatalogResource(
            identifier = "ulb",
            issued = "2024-01-01T00:00:00Z",
            modified = "2024-01-01T00:00:00Z",
            version = "1.0",
            subject = "Bible",
            formats = listOf(catalogFormat)
        )
        val catalogLanguage = CatalogLanguage(
            identifier = "en",
            resources = listOf(catalogResource)
        )
        val catalog = Catalog(languages = listOf(catalogLanguage))
        coEvery { catalogApi.getCatalog(any<String>()) } returns catalog

        // Setup mock resource container read
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
        coEvery { resourceContainerAccessor.read(any<kotlinx.io.files.Path>()) } returns resource

        val progressMessages = mutableListOf<String>()
        initApp { progressMessages.add(it) }

        // 1. Verify init flag is set to true in settings
        val finalInit = settings.getByName(DbSettings.INIT)?.value_?.toBoolean() ?: false
        assertTrue(finalInit)

        // 2. Verify languages are loaded
        val languages = languageDataSource.getAll()
        assertTrue(languages.isNotEmpty())
        // "en" should be one of the loaded languages
        assertNotNull(languages.find { it.slug == "en" })

        // 3. Verify resources are loaded
        val resources = resourceDataSource.getAll()
        assertTrue(resources.isNotEmpty())
        val enUlbResource = resourceDataSource.getByLangType("en", "ulb")
        assertNotNull(enUlbResource)
        assertEquals("en_ulb.zip", enUlbResource.filename)

        // 4. Verify clearTempDir was called
        coVerify { fileSystemProvider.clearTempDir() }
    }

    @Test
    fun testInitAppAlreadyInitialized() = runTest {
        // Mark as initialized
        settings.insert(DbSettings.INIT, "true")

        val progressMessages = mutableListOf<String>()
        initApp { progressMessages.add(it) }

        // Verify no languages or resources are loaded (database tables should remain empty)
        assertTrue(languageDataSource.getAll().isEmpty())
        assertTrue(resourceDataSource.getAll().isEmpty())

        // Verify clearTempDir was still called
        coVerify { fileSystemProvider.clearTempDir() }
    }
}
