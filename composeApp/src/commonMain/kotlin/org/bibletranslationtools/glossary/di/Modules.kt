package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.data.GlossaryDataSource
import org.bibletranslationtools.glossary.data.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.data.WordDataSource
import org.bibletranslationtools.glossary.data.WordDataSourceImpl
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.ui.viewmodel.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { GlossaryDatabase(createSqlDriver()) }
    singleOf(::GlossaryDataSourceImpl).bind<GlossaryDataSource>()
    singleOf(::WordDataSourceImpl).bind<WordDataSource>()

    factoryOf(::HomeViewModel)
}
