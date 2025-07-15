package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.domain.SettingsDataSource
import org.bibletranslationtools.glossary.domain.SettingsDataSourceImpl
import org.bibletranslationtools.glossary.domain.PhraseDataSource
import org.bibletranslationtools.glossary.domain.PhraseDataSourceImpl
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.DirectoryProviderImpl
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.RefDataSource
import org.bibletranslationtools.glossary.domain.RefDataSourceImpl
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSourceImpl
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SplashScreenModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { GlossaryDatabase(createSqlDriver()) }
    single { ResourceContainerAccessor(get()) }

    singleOf(::GlossaryDataSourceImpl).bind<GlossaryDataSource>()
    singleOf(::PhraseDataSourceImpl).bind<PhraseDataSource>()
    singleOf(::RefDataSourceImpl).bind<RefDataSource>()
    singleOf(::SettingsDataSourceImpl).bind<SettingsDataSource>()
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::WorkbookDataSourceImpl).bind<WorkbookDataSource>()
    singleOf(::InitApp)

    singleOf(::ReadScreenModel)
    singleOf(::SplashScreenModel)
}
