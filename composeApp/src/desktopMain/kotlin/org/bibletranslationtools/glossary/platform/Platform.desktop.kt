package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.bibletranslationtools.glossary.GlossaryDatabase
import java.io.File
import java.util.Locale
import java.util.Properties

actual val appDirPath: String
    get() {
        val propertyKey = "user.home"
        val appDirPath = "${System.getProperty(propertyKey)}/WaGlossary"
        val appDir = File(appDirPath)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir.canonicalPath
    }

actual fun applyLocale(iso: String) {
    val locale = Locale.of(iso)
    Locale.setDefault(locale)
}

actual fun createSqlDriver(): SqlDriver {
    val databasePath = File(appDirPath, "glossary.db")
    val properties = Properties()
    properties.setProperty("foreign_keys", "true")

    val driver: SqlDriver = JdbcSqliteDriver(
        url = "jdbc:sqlite:${databasePath.absolutePath}",
        properties = properties,
        schema = GlossaryDatabase.Schema
    )
    return driver
}