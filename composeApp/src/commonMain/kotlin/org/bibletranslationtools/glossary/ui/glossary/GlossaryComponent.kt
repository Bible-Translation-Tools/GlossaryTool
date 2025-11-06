package org.bibletranslationtools.glossary.ui.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.AppComponent
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.main.GlossaryIntent
import org.koin.core.component.KoinComponent

interface GlossaryComponent : ParentContext {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class GlossaryList(val component: GlossaryListComponent) : Child()
        class CreateGlossary(val component: CreateGlossaryComponent) : Child()
        class ViewChapter(val component: ViewChapterComponent) : Child()
        class ImportGlossary(val component: ImportGlossaryComponent) : Child()
        class SelectLanguage(val component: SelectLanguageComponent) : Child()
    }
}

@OptIn(DelicateDecomposeApi::class)
class DefaultGlossaryComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    intent: GlossaryIntent,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onSelectGlossary: (glossary: Glossary) -> Unit,
    private val onNavigateBack: () -> Unit
) : AppComponent(componentContext, parentContext),
    GlossaryComponent, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val createGlossaryState = instanceKeeper.getOrCreate { CreateGlossaryStateKeeper() }

    override val childStack: Value<ChildStack<*, GlossaryComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when (intent) {
                is GlossaryIntent.CreateGlossary -> Config.CreateGlossary
            },
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, context: ComponentContext): GlossaryComponent.Child {
        return when (config) {
            is Config.GlossaryList -> GlossaryComponent.Child.GlossaryList(
                DefaultGlossaryListComponent(
                    componentContext = context,
                    parentContext = parentContext,
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
                    parentContext = parentContext,
                    sharedState = createGlossaryState,
                    onNavigateBack = onNavigateBack,
                    onResourceDownloaded = onSelectResource,
                    onGlossaryCreated = { resource, glossary ->
                        onSelectResource(resource)
                        onSelectGlossary(glossary)
                        //navigation.replaceAll(Config.Index)
                    },
                    onSelectLanguage = { type ->
                        navigation.bringToFront(Config.SelectLanguage(type))
                    }
                )
            )
            is Config.ViewChapter -> GlossaryComponent.Child.ViewChapter(
                DefaultViewChapterComponent(
                    componentContext = context,
                    parentContext = parentContext,
                    phraseId = config.phraseId,
                    ref = config.ref,
                    onNavigateBack = {
                        if (childStack.value.backStack.isEmpty()) {
                            onNavigateBack()
                        } else navigation.pop()
                    }
                )
            )
            is Config.ImportGlossary -> GlossaryComponent.Child.ImportGlossary(
                DefaultImportGlossaryComponent(
                    componentContext = context,
                    parentContext = parentContext,
                    onSelectResource = onSelectResource,
                    onSelectGlossary = onSelectGlossary,
                    onImportFinished = {
                        //navigation.bringToFront(Config.Index)
                    },
                    onNavigateBack = navigation::pop
                )
            )
            is Config.SelectLanguage -> GlossaryComponent.Child.SelectLanguage(
                DefaultSelectLanguageComponent(
                    componentContext = context,
                    parentContext = parentContext,
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
        data object GlossaryList : Config
        @Serializable
        data object CreateGlossary : Config
        @Serializable
        data class ViewChapter(val phraseId: String, val ref: Ref) : Config
        @Serializable
        data object ImportGlossary : Config
        @Serializable
        data class SelectLanguage(val type: LanguageType) : Config
    }
}