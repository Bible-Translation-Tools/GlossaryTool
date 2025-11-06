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
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.main.DrawerContext

interface KeyTermsComponent : DrawerContext {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Index(val component: KeyTermsIndexComponent) : Child
        data class ViewPhrase(val component: ViewPhraseComponent) : Child
        data class EditPhrase(val component: EditPhraseComponent) : Child
        data class CreatePhrase(val component: CreatePhraseComponent) : Child
    }
}

class DefaultKeyTermsComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    private val book: Workbook,
    private val chapter: Chapter,
) : KeyTermsComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, KeyTermsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Index(book, chapter),
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
                    book = config.book,
                    chapter = config.chapter,
                    onNavigateImportGlossary = {
                    },
                    onNavigateGlossaryList = {
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
                    onNavigateRef = { phraseId, ref ->},
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
                        onPhraseSaved = {}
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
        }
    }

    override fun dismiss() {
        parentContext.dismissDrawer()
    }

    override fun navigateBack() {
        if (childStack.value.backStack.isNotEmpty()) {
            navigation.pop()
        } else {
            dismiss()
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data class Index(val book: Workbook, val chapter: Chapter) : Config
        @Serializable
        data class ViewPhrase(val phraseId: String) : Config
        @Serializable
        data class EditPhrase(val phrase: String) : Config
        @Serializable
        data object CreatePhrase : Config
    }
}