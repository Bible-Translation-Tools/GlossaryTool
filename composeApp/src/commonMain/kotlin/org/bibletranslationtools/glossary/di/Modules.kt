package org.bibletranslationtools.glossary.di

import org.bibletranslationtools.glossary.GlossaryDatabase
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.domain.DirectoryProvider
import org.bibletranslationtools.glossary.domain.DirectoryProviderImpl
import org.bibletranslationtools.glossary.domain.GlossaryDataSource
import org.bibletranslationtools.glossary.domain.GlossaryDataSourceImpl
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.domain.GlossaryRepositoryImpl
import org.bibletranslationtools.glossary.domain.InitApp
import org.bibletranslationtools.glossary.domain.PhraseDataSource
import org.bibletranslationtools.glossary.domain.PhraseDataSourceImpl
import org.bibletranslationtools.glossary.domain.RefDataSource
import org.bibletranslationtools.glossary.domain.RefDataSourceImpl
import org.bibletranslationtools.glossary.domain.SettingsDataSource
import org.bibletranslationtools.glossary.domain.SettingsDataSourceImpl
import org.bibletranslationtools.glossary.domain.WorkbookDataSource
import org.bibletranslationtools.glossary.domain.WorkbookDataSourceImpl
import org.bibletranslationtools.glossary.platform.ResourceContainerAccessor
import org.bibletranslationtools.glossary.platform.createSqlDriver
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SplashScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.TabbedScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateHolder
import org.bibletranslationtools.glossary.ui.state.AppStateHolderImpl
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
    singleOf(::GlossaryRepositoryImpl).bind<GlossaryRepository>()
    singleOf(::InitApp)
    singleOf(::AppStateHolderImpl).bind<AppStateHolder>()

    singleOf(::SplashScreenModel)
    singleOf(::TabbedScreenModel)
    singleOf(::GlossaryScreenModel)
    singleOf(::ReadScreenModel)
    factory { (phrase: Phrase) ->
        EditPhraseScreenModel(phrase, get(), get())
    }
}
