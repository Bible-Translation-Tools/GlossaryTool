package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Ref
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.domain.GlossaryRepository
import org.bibletranslationtools.glossary.ui.components.PhraseNavDir
import org.bibletranslationtools.glossary.ui.glossary.DefaultGlossaryComponent
import org.bibletranslationtools.glossary.ui.glossary.GlossaryComponent
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.bibletranslationtools.glossary.ui.read.DefaultReadComponent
import org.bibletranslationtools.glossary.ui.read.ReadComponent
import org.bibletranslationtools.glossary.ui.resources.DefaultResourcesComponent
import org.bibletranslationtools.glossary.ui.resources.ResourcesComponent
import org.bibletranslationtools.glossary.ui.settings.DefaultSettingsComponent
import org.bibletranslationtools.glossary.ui.settings.SettingsComponent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class PhraseDetails(
    val phrase: Phrase,
    val phrases: List<Phrase>,
    val ref: Ref?,
    val book: Workbook,
    val chapter: Chapter,
    val verse: String? = null
)

@Serializable
sealed class ReadIntent {
    @Serializable
    data object Index : ReadIntent()
    @Serializable
    data class Reference(val ref: RefOption) : ReadIntent()
}

@Serializable
sealed class GlossaryIntent {
    @Serializable
    data object Index : GlossaryIntent()
    @Serializable
    data class EditPhrase(val phrase: String) : GlossaryIntent()
    @Serializable
    data class ViewPhrase(val phrase: Phrase) : GlossaryIntent()
    @Serializable
    data object CreateGlossary : GlossaryIntent()
}

interface MainComponent {
    val model: Value<Model>

    data class Model(
        val phraseDetails: PhraseDetails? = null,
        val activeGlossary: Glossary? = null,
        val activeResource: Resource? = null
    )

    val childStack: Value<ChildStack<*, Child>>
    fun onTabClicked(tab: MainTab)
    fun navigatePhrase(dir: PhraseNavDir)
    fun navigateRef(dir: PhraseNavDir)
    fun clearPhraseDetails()
    fun onViewPhraseClick(phrase: Phrase)
    fun onEditPhraseClick(phrase: String)

    sealed class Child {
        class Read(val component: ReadComponent) : Child()
        class Glossary(val component: GlossaryComponent) : Child()
        class Resources(val component: ResourcesComponent) : Child()
        class Settings(val component: SettingsComponent) : Child()
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit
) : MainComponent, KoinComponent, ComponentContext by componentContext {

    private val appStateStore: AppStateStore by inject()
    private val glossaryRepository: GlossaryRepository by inject()

    private val _model = MutableValue(MainComponent.Model())
    override val model: Value<MainComponent.Model> = _model

    private val componentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Read(),
            handleBackButton = true,
            childFactory = ::createChild
        )

    private val backCallback = BackCallback(onBack = ::onNavigateBack, isEnabled = false)

    init {
        childStack.subscribe { stack ->
            backCallback.isEnabled = stack.backStack.isEmpty()
        }
        backHandler.register(backCallback)
    }

    private fun createChild(config: Config, context: ComponentContext): MainComponent.Child =
        when (config) {
            is Config.Read -> MainComponent.Child.Read(
                DefaultReadComponent(
                    componentContext = context,
                    intent = config.intent,
                    onPhraseDetails = ::loadPhrase,
                    onNavigateViewPhrase = ::onViewPhraseClick,
                    onNavigateEditPhrase = ::onEditPhraseClick
                )
            )
            is Config.Glossary -> MainComponent.Child.Glossary(
                DefaultGlossaryComponent(
                    componentContext = context,
                    intent = config.intent,
                    onNavigateRef = ::navigateToReadAndLoadRef,
                    onSelectResource = ::selectActiveResource,
                    onSelectGlossary = ::selectActiveGlossary,
                    onNavigateBack = navigation::pop
                )
            )
            is Config.Resources -> MainComponent.Child.Resources(
                DefaultResourcesComponent(context)
            )
            is Config.Settings -> MainComponent.Child.Settings(
                DefaultSettingsComponent(
                    componentContext = context,
                    onCreateGlossary = ::navigateToGlossaryCreate
                )
            )
        }

    override fun onTabClicked(tab: MainTab) {
        when (tab) {
            MainTab.Read -> navigation.replaceAll(Config.Read())
            MainTab.Glossary -> navigation.replaceAll(Config.Glossary())
            MainTab.Resources -> navigation.replaceAll(Config.Resources)
            MainTab.Settings -> navigation.replaceAll(Config.Settings)
        }
    }

    override fun navigatePhrase(dir: PhraseNavDir) {
        componentScope.launch {
            navigatePhrase(dir.value)
        }
    }

    override fun navigateRef(dir: PhraseNavDir) {
        componentScope.launch {
            navigateRef(dir.value)
        }
    }

    override fun clearPhraseDetails() {
        _model.update { it.copy(phraseDetails = null) }
    }

    override fun onViewPhraseClick(phrase: Phrase) {
        navigation.bringToFront(
            Config.Glossary(GlossaryIntent.ViewPhrase(phrase))
        )
    }

    override fun onEditPhraseClick(phrase: String) {
        navigation.bringToFront(
            Config.Glossary(GlossaryIntent.EditPhrase(phrase))
        )
    }

    private fun onNavigateBack() {
        val config = childStack.value.active.configuration
        when (config) {
            is Config.Glossary -> {
                val mainIntent = config.intent
                val lastConfig = childStack.value.backStack.lastOrNull()?.configuration

                if (mainIntent is GlossaryIntent.CreateGlossary && lastConfig is Config.Settings) {
                    navigation.pop()
                } else {
                    navigation.replaceAll(Config.Read())
                }
            }

            !is Config.Read -> {
                navigation.replaceAll(Config.Read())
            }

            else -> {
                navigation.replaceAll(Config.Read()) {
                    onFinished()
                }
            }
        }
    }

    private fun navigateToReadAndLoadRef(ref: RefOption) {
        navigation.replaceAll(Config.Read(
            ReadIntent.Reference(ref)
        ))
    }

    private fun navigateToGlossaryCreate() {
        navigation.bringToFront(
            Config.Glossary(GlossaryIntent.CreateGlossary)
        )
    }

    private fun selectActiveResource(resource: Resource) {
        _model.update { it.copy(activeResource = resource) }
        appStateStore.resourceStateHolder.updateResource(resource)
    }

    private fun selectActiveGlossary(glossary: Glossary) {
        _model.update { it.copy(activeGlossary = glossary) }
        appStateStore.glossaryStateHolder.updateGlossary(glossary)
    }

    private fun loadPhrase(
        phrase: Phrase,
        phrases: List<Phrase>,
        book: Workbook,
        chapter: Chapter,
        verse: String?
    ) {
        componentScope.launch {
            val ref = getInitialRef(
                phrase = phrase,
                book = book,
                chapter = chapter,
                verse = verse
            )
            val phraseDetails = PhraseDetails(
                phrase = phrase,
                phrases = phrases,
                ref = ref,
                book = book,
                chapter = chapter,
                verse = verse
            )

            _model.value = _model.value.copy(
                phraseDetails = phraseDetails
            )
        }
    }

    private suspend fun navigatePhrase(incr: Int) {
        _model.value.phraseDetails?.let { details ->
            details.phrases.getOrNull(
                details.phrases.indexOf(details.phrase) + incr
            )?.let { phrase ->
                val ref = getInitialRef(
                    phrase = phrase,
                    book = details.book,
                    chapter = details.chapter
                )
                _model.update { state ->
                    state.copy(
                        phraseDetails = details.copy(
                            phrase = phrase,
                            ref = ref
                        )
                    )
                }
            }
        }
    }

    private suspend fun getInitialRef(
        phrase: Phrase,
        book: Workbook,
        chapter: Chapter,
        verse: String? = null,
    ): Ref? {
        return withContext(Dispatchers.Default) {
            glossaryRepository.getRefs(phrase.id).firstOrNull {
                it.book == book.slug
                        && it.chapter == chapter.number.toString()
                        && (verse == null || it.verse == verse)
            }
        }
    }

    private suspend fun navigateRef(incr: Int) {
        withContext(Dispatchers.Default) {
            _model.value.phraseDetails?.let { details ->
                val refs = glossaryRepository.getRefs(details.phrase.id)
                refs.getOrNull(refs.indexOf(details.ref) + incr)
                    ?.let { ref ->
                        _model.update { state ->
                            state.copy(
                                phraseDetails = details.copy(
                                    ref = ref
                                )
                            )
                        }
                    }
            }
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data class Glossary(val intent: GlossaryIntent = GlossaryIntent.Index) : Config
        @Serializable
        data class Read(val intent: ReadIntent = ReadIntent.Index) : Config
        @Serializable
        data object Resources : Config
        @Serializable
        data object Settings : Config
    }
}
