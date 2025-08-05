package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.db.AfterVersion
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.init_catalog
import glossary.composeapp.generated.resources.init_languages
import glossary.composeapp.generated.resources.init_resources
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.toEntity
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.toLocalDateTime
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime

class InitApp(
    private val settings: SettingsDataSource,
    private val languageDataSource: LanguageDataSource,
    private val resourceDataSource: ResourceDataSource,
    private val directoryProvider: DirectoryProvider,
    private val resourceContainerAccessor: ResourceContainerAccessor,
    private val catalogApi: CatalogApi
) {
    suspend operator fun invoke(onProgressMessage: (String) -> Unit) {
        val driver = createSqlDriver()
        GlossaryDatabase.Schema.migrate(
            driver = driver,
            oldVersion = 0,
            newVersion = GlossaryDatabase.Schema.version,
            AfterVersion(1) {
                runBlocking {
                    // run migrations here
                }
            }
        )

        val init = settings.getByName(DbSettings.INIT)?.value_?.toBoolean() ?: false

        if (!init) {
            initLanguages(onProgressMessage)
            initCatalog(onProgressMessage)
            initResources(onProgressMessage)
            settings.insert(DbSettings.INIT, true.toString())
        }

        directoryProvider.clearTempDir()
    }

    private suspend fun initLanguages(onProgressMessage: (String) -> Unit) {
        onProgressMessage(getString(Res.string.init_languages))

        val bytes = Res.readBytes("files/langnames.json")
        val json = String(bytes)

        val languages = Utils.JsonLenient.decodeFromString<List<Language>>(json)

        languages.forEach { language ->
            languageDataSource.insert(language.toEntity())
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun initCatalog(onProgressMessage: (String) -> Unit) {
        onProgressMessage(getString(Res.string.init_catalog))

        val catalog = catalogApi.getCatalog("files/catalog.json")

        val excludedLanguages = listOf("el-x-koine")

        catalog.languages.filter { !excludedLanguages.contains(it.identifier) }
            .forEach { lang ->
                lang.resources.filter { it.subject.lowercase() == "bible" }
                .forEach { res ->
                    val resource = Resource(
                        lang = lang.identifier,
                        type = res.identifier,
                        version = res.version ?: "v1",
                        format = res.formats.first().format,
                         url = res.formats.first().url,
                        filename = "",
                        createdAt = res.issued.toLocalDateTime(),
                        modifiedAt = res.modified.toLocalDateTime()
                    )
                    try {
                        resourceDataSource.insert(resource.toEntity())
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            }
    }

    private suspend fun initResources(onProgressMessage: (String) -> Unit) {
        onProgressMessage(getString(Res.string.init_resources))

        val bytes = Res.readBytes("files/en_ulb.zip")
        val resourcePath = directoryProvider.saveSource(bytes, "en_ulb.zip")
        resourceContainerAccessor.read(resourcePath)?.let { resource ->
            resourceDataSource.getByLangType("en", "ulb")?.let { dbRes ->
                resourceDataSource.insert(
                    resource.copy(
                        url = dbRes.url,
                    ).toEntity()
                )
            }
        }
    }
}