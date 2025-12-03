package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.domain.CatalogApi
import org.bibletranslationtools.glossary.domain.CatalogApiImpl
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.DirectoryProviderImpl
import org.bibletranslationtools.glossary.domain.GlossaryApi
import org.bibletranslationtools.glossary.domain.GlossaryApiImpl
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.createHttpClient
import org.bibletranslationtools.glossary.domain.data.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.data.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.domain.data.GlossaryRepository
import org.bibletranslationtools.glossary.domain.data.GlossaryRepositoryImpl
import org.bibletranslationtools.glossary.domain.data.LanguageDataSource
import org.bibletranslationtools.glossary.domain.data.LanguageDataSourceImpl
import org.bibletranslationtools.glossary.domain.data.PhraseDataSource
import org.bibletranslationtools.glossary.domain.data.PhraseDataSourceImpl
import org.bibletranslationtools.glossary.domain.data.ResourceDataSource
import org.bibletranslationtools.glossary.domain.data.ResourceDataSourceImpl
import org.bibletranslationtools.glossary.domain.data.SettingsDataSource
import org.bibletranslationtools.glossary.domain.data.SettingsDataSourceImpl
import org.bibletranslationtools.glossary.domain.usecases.ExportGlossary
import org.bibletranslationtools.glossary.domain.usecases.ImportGlossary
import org.bibletranslationtools.glossary.domain.usecases.MergePendingPhrases
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.platform.httpClientEngine
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.bibletranslationtools.glossary.ui.state.AppStateStoreImpl
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolder
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolderImpl
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolder
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolderImpl
import org.bibletranslationtools.glossary.ui.state.UserStateHolder
import org.bibletranslationtools.glossary.ui.state.UserStateHolderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { GlossaryDatabase(createSqlDriver()) }
    single { ResourceContainerAccessor(get()) }
    single { CatalogApiImpl(createHttpClient(httpClientEngine)) }.bind<CatalogApi>()
    single { GlossaryApiImpl(createHttpClient(httpClientEngine)) }.bind<GlossaryApi>()

    singleOf(::GlossaryDataSourceImpl).bind<GlossaryDataSource>()
    singleOf(::PhraseDataSourceImpl).bind<PhraseDataSource>()
    singleOf(::SettingsDataSourceImpl).bind<SettingsDataSource>()
    singleOf(::LanguageDataSourceImpl).bind<LanguageDataSource>()
    singleOf(::ResourceDataSourceImpl).bind<ResourceDataSource>()
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::GlossaryRepositoryImpl).bind<GlossaryRepository>()
    singleOf(::ExportGlossary)
    singleOf(::ImportGlossary)
    singleOf(::MergePendingPhrases)

    factoryOf(::InitApp)

    singleOf(::ResourceStateHolderImpl).bind<ResourceStateHolder>()
    singleOf(::GlossaryStateHolderImpl).bind<GlossaryStateHolder>()
    singleOf(::UserStateHolderImpl).bind<UserStateHolder>()
    singleOf(::AppStateStoreImpl).bind<AppStateStore>()
}
