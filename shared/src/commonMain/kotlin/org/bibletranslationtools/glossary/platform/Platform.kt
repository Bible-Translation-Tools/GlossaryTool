package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.HttpClientEngine

expect val appDirPath: String
expect val httpClientEngine: HttpClientEngine
expect fun applyLocale(iso: String)
expect fun createSqlDriver(): SqlDriver
expect fun showStatusBars(show: Boolean)
expect fun showNavigationBar(show: Boolean)
expect fun setStatusBarLight(light: Boolean)
