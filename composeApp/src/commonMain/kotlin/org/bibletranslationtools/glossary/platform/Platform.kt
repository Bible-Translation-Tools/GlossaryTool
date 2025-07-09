package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver

expect val appDirPath: String
expect fun applyLocale(iso: String)

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}