package org.bibletranslationtools.glossary

import android.app.Application
import org.bibletranslationtools.glossary.di.initKoin
import org.bibletranslationtools.glossary.platform.ActivityTracker
import org.koin.android.ext.koin.androidContext

class GlossaryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ActivityTracker.install(this)

        AppLogger.setupGlobalExceptionHandler()
        
        initKoin {
            androidContext(applicationContext)
        }
    }
}
