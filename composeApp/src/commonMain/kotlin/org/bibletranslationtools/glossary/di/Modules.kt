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
import org.bibletranslationtools.glossary.domain.LanguageDataSource
import org.bibletranslationtools.glossary.domain.LanguageDataSourceImpl
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
import org.bibletranslationtools.glossary.ui.screenmodel.CreateGlossaryScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryListScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.ImportGlossaryScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.ReadScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SearchPhraseScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SelectLanguageScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SharedScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.SplashScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.ViewPhraseScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.bibletranslationtools.glossary.ui.state.AppStateStoreImpl
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolder
import org.bibletranslationtools.glossary.ui.state.GlossaryStateHolderImpl
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolder
import org.bibletranslationtools.glossary.ui.state.ResourceStateHolderImpl
import org.bibletranslationtools.glossary.ui.state.TabStateHolder
import org.bibletranslationtools.glossary.ui.state.TabStateHolderImpl
import org.koin.core.module.dsl.factoryOf
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
    singleOf(::LanguageDataSourceImpl).bind<LanguageDataSource>()
    singleOf(::DirectoryProviderImpl).bind<DirectoryProvider>()
    singleOf(::WorkbookDataSourceImpl).bind<WorkbookDataSource>()
    singleOf(::GlossaryRepositoryImpl).bind<GlossaryRepository>()
    factoryOf(::InitApp)

    singleOf(::ResourceStateHolderImpl).bind<ResourceStateHolder>()
    singleOf(::GlossaryStateHolderImpl).bind<GlossaryStateHolder>()
    singleOf(::TabStateHolderImpl).bind<TabStateHolder>()
    singleOf(::AppStateStoreImpl).bind<AppStateStore>()

    factoryOf(::SplashScreenModel)
    factoryOf(::SharedScreenModel)
    factoryOf(::GlossaryScreenModel)
    factoryOf(::ReadScreenModel)
    factoryOf(::SearchPhraseScreenModel)
    factoryOf(::SelectLanguageScreenModel)
    factoryOf(::CreateGlossaryScreenModel)
    factoryOf(::GlossaryListScreenModel)
    factoryOf(::ImportGlossaryScreenModel)
    factory { (phrase: String) ->
        EditPhraseScreenModel(phrase, get(), get())
    }
    factory { (phrase: Phrase) ->
        ViewPhraseScreenModel(phrase, get())
    }
}
