package org.bibletranslationtools.glossary.di

import app.cash.sqldelight.db.SqlDriver
import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.persistence.GlossaryDataSource
import org.bibletranslationtools.glossary.persistence.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.persistence.WordDataSource
import org.bibletranslationtools.glossary.persistence.WordDataSourceImpl
import org.bibletranslationtools.glossary.platform.DatabaseDriverFactory
import org.bibletranslationtools.glossary.ui.viewmodel.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { GlossaryDatabase(get()) }
    single<SqlDriver> { get<DatabaseDriverFactory>().create() }
    singleOf(::GlossaryDataSourceImpl).bind<GlossaryDataSource>()
    singleOf(::WordDataSourceImpl).bind<WordDataSource>()

    factoryOf(::HomeViewModel)
}
