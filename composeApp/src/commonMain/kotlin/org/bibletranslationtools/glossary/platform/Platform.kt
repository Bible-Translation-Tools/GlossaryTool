package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.engine.HttpClientEngine
import kotlinx.io.files.Path

expect val appDirPath: String
expect val httpClientEngine: HttpClientEngine
expect fun applyLocale(iso: String)
expect fun createSqlDriver(): SqlDriver
expect fun extractZip(file: PlatformFile, destDir: Path)
expect fun zipDirectory(source: Path, target: PlatformFile)
