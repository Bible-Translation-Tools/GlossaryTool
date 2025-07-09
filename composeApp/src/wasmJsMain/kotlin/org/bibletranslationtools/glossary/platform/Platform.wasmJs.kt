package org.bibletranslationtools.glossary.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import org.w3c.dom.Worker

actual val appDirPath: String get() = "/"

actual fun applyLocale(iso: String) {
    println("Applying locale not implemented in wasmJs yet")
}

fun jsWorker(): Worker = js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val driver = WebWorkerDriver(jsWorker())
        return driver
    }
}