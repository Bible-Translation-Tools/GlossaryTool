package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.main.GlossaryIntent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GlossaryComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class GlossaryIndex(val component: GlossaryIndexComponent) : Child()
        class GlossaryList(val component: GlossaryListComponent) : Child()
        class CreateGlossary(val component: CreateGlossaryComponent) : Child()
        class EditPhrase(val component: EditPhraseComponent) : Child()
        class ViewPhrase(val component: ViewPhraseComponent) : Child()
        class ImportGlossary(val component: ImportGlossaryComponent) : Child()
        class SearchPhrases(val component: SearchPhrasesComponent) : Child()
        class SelectLanguage(val component: SelectLanguageComponent) : Child()
    }
}

@OptIn(DelicateDecomposeApi::class)
class DefaultGlossaryComponent(
    componentContext: ComponentContext,
    intent: GlossaryIntent,
    private val onNavigateRef: (RefOption) -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onSelectGlossary: (glossary: Glossary) -> Unit,
    private val onNavigateBack: () -> Unit
) : GlossaryComponent, KoinComponent, ComponentContext by componentContext {

    private val appStateStore: AppStateStore by inject()
    private val navigation = StackNavigation<Config>()
    private val createGlossaryState = instanceKeeper.getOrCreate { CreateGlossaryStateKeeper() }

    override val childStack: Value<ChildStack<*, GlossaryComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when (intent) {
                is GlossaryIntent.Index -> Config.Index
                is GlossaryIntent.ViewPhrase -> Config.ViewPhrase(intent.phrase)
                is GlossaryIntent.EditPhrase -> Config.EditPhrase(intent.phrase)
                is GlossaryIntent.CreateGlossary -> Config.CreateGlossary
            },
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, context: ComponentContext): GlossaryComponent.Child {
        return when (config) {
            is Config.Index -> GlossaryComponent.Child.GlossaryIndex(
                DefaultGlossaryIndexComponent(
                    componentContext = context,
                    onNavigateImportGlossary = {
                        navigation.bringToFront(Config.ImportGlossary)
                    },
                    onNavigateGlossaryList = {
                        navigation.bringToFront(Config.GlossaryList)
                    },
                    onNavigateSearchPhrases = {
                        navigation.bringToFront(Config.SearchPhrases)
                    },
                    onNavigateViewPhrase = { phrase ->
                        navigation.bringToFront(Config.ViewPhrase(phrase))
                    }
                )
            )
            is Config.GlossaryList -> GlossaryComponent.Child.GlossaryList(
                DefaultGlossaryListComponent(
                    componentContext = context,
                    onNavigateImportGlossary = {
                        navigation.bringToFront(Config.ImportGlossary)
                    },
                    onSelectResource = onSelectResource,
                    onSelectGlossary = onSelectGlossary,
                    onNavigateBack = navigation::pop
                )
            )
            is Config.CreateGlossary -> GlossaryComponent.Child.CreateGlossary(
                DefaultCreateGlossaryComponent(
                    componentContext = context,
                    sharedState = createGlossaryState,
                    onNavigateBack = onNavigateBack,
                    onResourceDownloaded = {
                        appStateStore.resourceStateHolder.updateResource(it)
                    },
                    onGlossaryCreated = { resource, glossary ->
                        onSelectResource(resource)
                        onSelectGlossary(glossary)
                        navigation.replaceAll(Config.Index)
                    },
                    onSelectLanguage = { type ->
                        navigation.bringToFront(Config.SelectLanguage(type))
                    }
                )
            )
            is Config.ViewPhrase -> GlossaryComponent.Child.ViewPhrase(
                DefaultViewPhraseComponent(
                    componentContext = context,
                    phrase = config.phrase,
                    onNavigateBack = navigation::pop,
                    onNavigateRef = onNavigateRef,
                    onNavigateEdit = { phrase ->
                        navigation.bringToFront(Config.EditPhrase(phrase))
                    }
                )
            )
            is Config.EditPhrase -> GlossaryComponent.Child.EditPhrase(
                DefaultEditPhraseComponent(
                    componentContext = context,
                    phrase = config.phrase,
                    onPhraseSaved = navigation::pop,
                    onNavigateBack = navigation::pop
                )
            )
            is Config.ImportGlossary -> GlossaryComponent.Child.ImportGlossary(
                DefaultImportGlossaryComponent(
                    componentContext = context,
                    onNavigateBack = navigation::pop
                )
            )
            is Config.SearchPhrases -> GlossaryComponent.Child.SearchPhrases(
                DefaultSearchPhrasesComponent(
                    componentContext = context,
                    onNavigateBack = navigation::pop,
                    onNavigateEdit = { phrase ->
                        navigation.bringToFront(Config.EditPhrase(phrase))
                    }
                )
            )
            is Config.SelectLanguage -> GlossaryComponent.Child.SelectLanguage(
                DefaultSelectLanguageComponent(
                    componentContext = context,
                    type = config.type,
                    sharedState = createGlossaryState,
                    onDismiss = navigation::pop
                )
            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Index : Config
        @Serializable
        data object GlossaryList : Config
        @Serializable
        data object CreateGlossary : Config
        @Serializable
        data class ViewPhrase(val phrase: Phrase) : Config
        @Serializable
        data class EditPhrase(val phrase: String) : Config
        @Serializable
        data object ImportGlossary : Config
        @Serializable
        data object SearchPhrases : Config
        @Serializable
        data class SelectLanguage(val type: LanguageType) : Config
    }
}