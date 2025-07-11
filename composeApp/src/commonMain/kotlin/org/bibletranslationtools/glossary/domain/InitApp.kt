package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.db.AfterVersion
import glossary.composeapp.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.platform.extractZip

class InitApp(
    private val settings: SettingsDataSource,
    private val directoryProvider: DirectoryProvider,
    private val database: GlossaryDatabase
) {
    suspend operator fun invoke() {
        val driver = createSqlDriver()
        GlossaryDatabase.Schema.migrate(
            driver = driver,
            oldVersion = 0,
            newVersion = GlossaryDatabase.Schema.version,
            AfterVersion(0) {
                runBlocking {
                    settings.insert(DbSettings.INIT.value, false.toString())
                }
            }
        )

        val init = settings.getByName(DbSettings.INIT.value)?.value_?.toBoolean() ?: false

        if (!init) {
            initResources()
            settings.insert(DbSettings.INIT.value, true.toString())
        }
    }

    private suspend fun initResources() {
        val bytes = Res.readBytes("files/en_ulb.zip")
        extractZip(bytes, directoryProvider.sources)
    }
}