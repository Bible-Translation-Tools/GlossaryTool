package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import kotlinx.io.files.Path
import org.bibletranslationtools.glossary.data.Workbook

expect val appDirPath: String
expect fun applyLocale(iso: String)
expect fun createSqlDriver(): SqlDriver
expect fun extractZip(bytes: ByteArray, dir: Path)
expect fun readResourceContainer(
    language: String,
    resource: String
): List<Workbook>
