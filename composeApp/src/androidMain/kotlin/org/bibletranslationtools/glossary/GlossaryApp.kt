package org.bibletranslationtools.glossary

import android.app.Application
import org.bibletranslationtools.glossary.di.initKoin
import org.koin.android.ext.koin.androidContext

class GlossaryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AppLogger.setupGlobalExceptionHandler()
        
        initKoin {
            androidContext(applicationContext)
        }
    }
}