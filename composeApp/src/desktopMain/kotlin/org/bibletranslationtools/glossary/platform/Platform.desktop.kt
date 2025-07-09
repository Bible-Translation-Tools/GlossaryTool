package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Locale

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

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val databasePath = File(appDirPath, "glossary.db")
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        return driver
    }
}