package org.bibletranslationtools.glossary.ui.drawer.keyterms

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.main.KeyTermsIntent
import org.bibletranslationtools.glossary.ui.main.MainStateKeeper

interface KeyTermsComponent : DrawerContext {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class KeyTerms(val component: KeyTermsListComponent) : Child
        data class ViewPhrase(val component: ViewPhraseComponent) : Child
        data class EditPhrase(val component: EditPhraseComponent) : Child
        data class CreatePhrase(val component: CreatePhraseComponent) : Child
        data class ViewChapter(val component: ViewChapterComponent) : Child
    }
}

class DefaultKeyTermsComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    intent: KeyTermsIntent,
    private val sharedState: MainStateKeeper,
    private val onFullscreen: (Boolean) -> Unit,
    private val onNavigateImportGlossary: () -> Unit,
    private val onNavigateCreateGlossary: () -> Unit,
    private val onSelectResource: (resource: Resource) -> Unit,
    private val onSelectGlossary: (glossary: Glossary, openKeyTerms: Boolean) -> Unit,
) : KeyTermsComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, KeyTermsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when (intent) {
                is KeyTermsIntent.Index -> Config.KeyTerms
                is KeyTermsIntent.ViewPhrase -> Config.ViewPhrase(intent.phrase)
                is KeyTermsIntent.EditPhrase -> Config.EditPhrase(intent.phrase)
            },
            handleBackButton = false,
            childFactory = ::createChild
        )

    init {
        backHandler.register(
            BackCallback {
                navigateBack()
            }
        )
    }

    private fun createChild(
        config: Config,
        context: ComponentContext
    ) : KeyTermsComponent.Child {
        return when (config) {
            is Config.KeyTerms -> KeyTermsComponent.Child.KeyTerms(
                DefaultKeyTermsListComponent(
                    componentContext = context,
                    parentContext = this,
                    onNavigateImportGlossary = {
                        onNavigateImportGlossary()
                    },
                    onNavigateCreateGlossary = {
                        onNavigateCreateGlossary()
                    },
                    onNavigateSearchPhrases = {
                        navigation.bringToFront(Config.CreatePhrase)
                    },
                    onNavigateViewPhrase = { phrase ->
                        navigation.bringToFront(Config.ViewPhrase(phrase))
                    },
                    onSelectResource = onSelectResource,
                    onSelectGlossary = onSelectGlossary,
                    onTriggerUpdate = {
                        sharedState.setTriggerUpdate(true)
                    }
                )
            )
            is Config.ViewPhrase -> KeyTermsComponent.Child.ViewPhrase(
                DefaultViewPhraseComponent(
                    componentContext = context,
                    parentContext = this,
                    phrase = config.phrase,
                    onNavigateRef = { phrase, ref ->
                        navigation.bringToFront(Config.ViewChapter(phrase, ref))
                    },
                    onNavigateEdit = {
                        navigation.bringToFront(Config.EditPhrase(it))
                    }
                )
            )
            is Config.EditPhrase -> KeyTermsComponent.Child.EditPhrase(
                    DefaultEditPhraseComponent(
                        componentContext = context,
                        parentContext = this,
                        phrase = config.phrase,
                        onPhraseSaved = {
                            sharedState.setTriggerUpdate(true)
                            navigateBack()
                        }
                    )
                )
            is Config.CreatePhrase -> KeyTermsComponent.Child.CreatePhrase(
                DefaultCreatePhraseComponent(
                    componentContext = context,
                    parentContext = this,
                    onNavigateEdit = {
                        navigation.bringToFront(Config.EditPhrase(it))
                    }
                )
            )
            is Config.ViewChapter -> KeyTermsComponent.Child.ViewChapter(
                DefaultViewChapterComponent(
                    componentContext = context,
                    parentContext = this,
                    phrase = config.phrase,
                    ref = config.ref
                )
            )
        }
    }

    override fun dismiss() {
        setFullscreen(false)
        parentContext.dismissDrawer()
    }

    override fun navigateBack() {
        setFullscreen(false)
        if (childStack.value.backStack.isNotEmpty()) {
            navigation.pop()
        } else {
            dismiss()
        }
    }

    override fun setFullscreen(fullscreen: Boolean) {
        onFullscreen(fullscreen)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object KeyTerms : Config
        @Serializable
        data class ViewPhrase(val phrase: Phrase) : Config
        @Serializable
        data class EditPhrase(val phrase: Phrase) : Config
        @Serializable
        data object CreatePhrase : Config
        @Serializable
        data class ViewChapter(val phrase: Phrase, val ref: Ref) : Config
    }
}