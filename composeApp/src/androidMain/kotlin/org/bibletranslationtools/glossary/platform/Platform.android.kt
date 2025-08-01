package org.bibletranslationtools.glossary.platform

import android.content.Context
import android.os.LocaleList
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
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
    val locale = Locale.forLanguageTag(iso)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocales(LocaleList(locale))
}

actual fun createSqlDriver(): SqlDriver =
    AndroidSqliteDriver(
        schema = GlossaryDatabase.Schema,
        context = getKoin().get(),
        name = "glossary.db",
        callback = object : AndroidSqliteDriver.Callback(GlossaryDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=ON;")
            }
        }
    )

actual val httpClientEngine: HttpClientEngine
    get() = Android.create()