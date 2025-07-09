package org.bibletranslationtools.glossary.platform

import android.content.Context
import android.os.LocaleList
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.koin.mp.KoinPlatform.getKoin
import java.util.Locale

actual val appDirPath: String
    get() {
        val context: Context = getKoin().get()
        return context.getExternalFilesDir(null)?.canonicalPath
            ?: throw IllegalArgumentException("External files dir not found")
    }

actual fun applyLocale(iso: String) {
    val context: Context = getKoin().get()
    val locale = Locale(iso)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocales(LocaleList(locale))
}

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            GlossaryDatabase.Schema.synchronous(),
            context,
            "glossary.db"
        )
}