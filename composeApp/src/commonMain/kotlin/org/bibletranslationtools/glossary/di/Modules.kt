package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.domain.CatalogApi
import org.bibletranslationtools.glossary.domain.CatalogApiImpl
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.DirectoryProviderImpl
import org.bibletranslationtools.glossary.domain.ExportGlossary
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.GlossaryRepositoryImpl
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.LanguageDataSource
import org.bibletranslationtools.glossary.domain.LanguageDataSourceImpl
import org.bibletranslationtools.glossary.domain.PhraseDataSource
import org.bibletranslationtools.glossary.domain.PhraseDataSourceImpl
import org.bibletranslationtools.glossary.domain.RefDataSource
import org.bibletranslationtools.glossary.domain.RefDataSourceImpl
import org.bibletranslationtools.glossary.domain.ResourceDataSource
import org.bibletranslationtools.glossary.domain.ResourceDataSourceImpl
import org.bibletranslationtools.glossary.domain.SettingsDataSource
import org.bibletranslationtools.glossary.domain.SettingsDataSourceImpl
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSourceImpl
import org.bibletranslationtools.glossary.domain.createHttpClient
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.platform.httpClientEngine
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.bibletranslationtools.glossary.ui.state.AppStateStoreImpl
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolder
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolderImpl
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolder
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { GlossaryDatabase(createSqlDriver()) }
    single { ResourceContainerAccessor(get()) }
    single { CatalogApiImpl(createHttpClient(httpClientEngine)) }.bind<CatalogApi>()

    singleOf(::GlossaryDataSourceImpl).bind<GlossaryDataSource>()
    singleOf(::PhraseDataSourceImpl).bind<PhraseDataSource>()
    singleOf(::RefDataSourceImpl).bind<RefDataSource>()
    singleOf(::SettingsDataSourceImpl).bind<SettingsDataSource>()
    singleOf(::LanguageDataSourceImpl).bind<LanguageDataSource>()
    singleOf(::ResourceDataSourceImpl).bind<ResourceDataSource>()
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::WorkbookDataSourceImpl).bind<WorkbookDataSource>()
    singleOf(::GlossaryRepositoryImpl).bind<GlossaryRepository>()
    singleOf(::ExportGlossary)

    factoryOf(::InitApp)

    singleOf(::ResourceStateHolderImpl).bind<ResourceStateHolder>()
    singleOf(::GlossaryStateHolderImpl).bind<GlossaryStateHolder>()
    singleOf(::AppStateStoreImpl).bind<AppStateStore>()
}
