package org.bibletranslationtools.glossary.platform

import android.content.Context
import android.os.LocaleList
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.WindowInsetsControllerCompat
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
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
        factory = RequerySQLiteOpenHelperFactory(),
        callback = object : AndroidSqliteDriver.Callback(GlossaryDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=ON;")
            }
        }
    )

actual val httpClientEngine: HttpClientEngine
    get() = Android.create()

actual fun showStatusBars(show: Boolean) {
    showBars(WindowInsetsCompat.Type.statusBars(), show)
}

actual fun showNavigationBar(show: Boolean) {
    showBars(WindowInsetsCompat.Type.navigationBars(), show)
}

actual fun setStatusBarLight(light: Boolean) {
    getInsetsController()?.apply {
        isAppearanceLightStatusBars = light
    }
}

private fun showBars(@InsetsType type: Int, show: Boolean) {
    getInsetsController()?.apply {
        if (show) {
            show(type)
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(type)
        }
    }
}

private fun getInsetsController(): WindowInsetsControllerCompat? {
    val activity = ActivityTracker.activity ?: return null
    val insetsController = WindowCompat.getInsetsController(
        activity.window,
        activity.window.decorView
    )
    return insetsController
}
