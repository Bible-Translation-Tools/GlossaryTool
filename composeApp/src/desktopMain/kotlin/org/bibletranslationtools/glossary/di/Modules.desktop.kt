package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.platform.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::DatabaseDriverFactory)
}
