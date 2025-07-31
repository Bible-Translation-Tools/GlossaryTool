package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.db.AfterVersion
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.init_languages
import glossary.composeapp.generated.resources.init_resources
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.data.toEntity
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.jetbrains.compose.resources.getString

class InitApp(
    private val settings: SettingsDataSource,
    private val languageDataSource: LanguageDataSource,
    private val resourceDataSource: ResourceDataSource,
    private val directoryProvider: DirectoryProvider,
    private val resourceContainerAccessor: ResourceContainerAccessor
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

        val init = settings.getByName(DbSettings.INIT.value)?.value_?.toBoolean() ?: false

        if (!init) {
            initLanguages(onProgressMessage)
            initResources(onProgressMessage)
            settings.insert(DbSettings.INIT.value, true.toString())
        }
    }

    private suspend fun initResources(onProgressMessage: (String) -> Unit) {
        onProgressMessage(getString(Res.string.init_resources))

        val bytes = Res.readBytes("files/en_ulb.zip")
        val resourcePath = directoryProvider.saveSource(bytes, "en_ulb.zip")
        val resource = resourceContainerAccessor.read(resourcePath)

        resourceDataSource.insert(resource.toEntity())
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
}