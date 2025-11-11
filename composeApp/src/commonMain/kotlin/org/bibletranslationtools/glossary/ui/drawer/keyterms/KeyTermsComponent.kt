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
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.main.KeyTermsIntent
import org.bibletranslationtools.glossary.ui.main.MainStateKeeper

interface KeyTermsComponent : DrawerContext {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Index(val component: KeyTermsIndexComponent) : Child
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
    private val onNavigateCreateGlossary: () -> Unit
) : KeyTermsComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, KeyTermsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when (intent) {
                is KeyTermsIntent.Index -> Config.Index
                is KeyTermsIntent.ViewPhrase -> Config.ViewPhrase(intent.phraseId)
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
            is Config.Index -> KeyTermsComponent.Child.Index(
                DefaultKeyTermsIndexComponent(
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
                    onNavigateViewPhrase = { phraseId ->
                        navigation.bringToFront(Config.ViewPhrase(phraseId))
                    }
                )
            )
            is Config.ViewPhrase -> KeyTermsComponent.Child.ViewPhrase(
                DefaultViewPhraseComponent(
                    componentContext = context,
                    parentContext = this,
                    phraseId = config.phraseId,
                    onNavigateRef = { phraseId, ref ->
                        navigation.bringToFront(Config.ViewChapter(phraseId, ref))
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
                            sharedState.updatePhraseUpdated(true)
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
                    phraseId = config.phraseId,
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
        data object Index : Config
        @Serializable
        data class ViewPhrase(val phraseId: String) : Config
        @Serializable
        data class EditPhrase(val phrase: String) : Config
        @Serializable
        data object CreatePhrase : Config
        @Serializable
        data class ViewChapter(val phraseId: String, val ref: Ref) : Config
    }
}