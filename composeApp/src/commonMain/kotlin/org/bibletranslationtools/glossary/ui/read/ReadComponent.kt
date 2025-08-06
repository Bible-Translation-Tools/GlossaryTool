package org.bibletranslationtools.glossary.ui.read

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.main.ParentContext
import org.bibletranslationtools.glossary.ui.main.ReadIntent
import org.bibletranslationtools.glossary.ui.main.AppComponent
import org.koin.core.component.KoinComponent

interface ReadComponent: ParentContext {
    val model: Value<Model>

    data class Model(
        val currentRef: RefOption? = null
    )

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Index(val component: ReadIndexComponent) : Child()
        class Browse(val component: BrowseComponent) : Child()
    }
}

class DefaultReadComponent(
    componentContext: ComponentContext,
    private val parentContext: ParentContext,
    intent: ReadIntent,
    private val onNavigateViewPhrase: (phrase: Phrase) -> Unit,
    private val onPhraseDetails: (
        phrase: Phrase,
        phrases: List<Phrase>,
        book: Workbook,
        chapter: Chapter,
        verse: String?
    ) -> Unit,
    private val onNavigateEditPhrase: (String) -> Unit
) : AppComponent(componentContext, parentContext),
    ReadComponent, KoinComponent {

    private val _model = MutableValue(ReadComponent.Model())
    override val model: Value<ReadComponent.Model> = _model

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, ReadComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = when (intent) {
                is ReadIntent.Reference -> {
                    Config.Index(intent.ref)
                }
                else -> Config.Index()
            },
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, context: ComponentContext): ReadComponent.Child =
        when (config) {
            is Config.Index -> ReadComponent.Child.Index(
                DefaultReadIndexComponent(
                    componentContext = context,
                    parentContext = parentContext,
                    ref = config.ref,
                    onNavigateViewPhrase = onNavigateViewPhrase,
                    onNavigateEditPhrase = onNavigateEditPhrase,
                    onPhraseSelected = onPhraseDetails,
                    onNavigateBrowse = { book, chapter ->
                        navigation.bringToFront(Config.Browse(book, chapter))
                    }
                )
            )
            is Config.Browse -> ReadComponent.Child.Browse(
                DefaultBrowseComponent(
                    componentContext = context,
                    parentContext = parentContext,
                    book = config.book,
                    chapter = config.chapter,
                    onNavigateRef = {
                        navigation.replaceAll(Config.Index(it))
                    },
                    onNavigateBack = { navigation.pop() }
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data class Index(val ref: RefOption? = null) : Config
        @Serializable
        data class Browse(val book: String, val chapter: Int) : Config
    }
}