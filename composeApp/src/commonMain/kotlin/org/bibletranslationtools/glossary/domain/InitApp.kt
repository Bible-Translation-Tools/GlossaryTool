package org.bibletranslationtools.glossary.domain

import app.cash.sqldelight.db.AfterVersion
import glossary.composeapp.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.platform.createSqlDriver

class InitApp(
    private val settings: SettingsDataSource,
    private val directoryProvider: DirectoryProvider
) {
    suspend operator fun invoke() {
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
            initResources()
            settings.insert(DbSettings.INIT.value, true.toString())
        }
    }

    private suspend fun initResources() {
        val bytes = Res.readBytes("files/en_ulb.zip")
        directoryProvider.saveSource(bytes, "en_ulb.zip")
    }
}