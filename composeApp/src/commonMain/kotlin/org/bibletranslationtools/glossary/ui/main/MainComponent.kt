package org.bibletranslationtools.glossary.ui.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Chapter
import org.bibletranslationtools.glossary.data.Glossary
import org.bibletranslationtools.glossary.data.RefOption
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.data.Workbook
import org.bibletranslationtools.glossary.ui.ParentContext
import org.bibletranslationtools.glossary.ui.drawer.DrawerContext
import org.bibletranslationtools.glossary.ui.drawer.keyterms.DefaultKeyTermsComponent
import org.bibletranslationtools.glossary.ui.drawer.settings.DefaultSettingsComponent
import org.bibletranslationtools.glossary.ui.read.DefaultReadComponent
import org.bibletranslationtools.glossary.ui.read.ReadComponent
import org.bibletranslationtools.glossary.ui.resources.DefaultResourcesComponent
import org.bibletranslationtools.glossary.ui.resources.ResourcesComponent
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
sealed class ReadIntent {
    @Serializable
    data object Index : ReadIntent()
    @Serializable
    data class Reference(val ref: RefOption) : ReadIntent()
}

@Serializable
sealed class KeyTermsIntent {
    @Serializable
    data class Index(val book: Workbook, val chapter: Chapter) : KeyTermsIntent()
    @Serializable
    data class ViewPhrase(val phraseId: String) : KeyTermsIntent()
    @Serializable
    data class EditPhrase(val phrase: String): KeyTermsIntent()
}

@Serializable
sealed interface DrawerConfig {
    @Serializable
    data object Settings : DrawerConfig
    @Serializable
    data class KeyTerms(val intent: KeyTermsIntent) : DrawerConfig
}

interface MainComponent: ParentContext {
    val model: Value<Model>

    data class Model(
        val activeGlossary: Glossary? = null,
        val activeResource: Resource? = null,
        val fullscreenDrawer: Boolean = false,
        val phraseUpdated: Boolean = false
    )

    val childStack: Value<ChildStack<*, Child>>
    val drawerSlot: Value<ChildSlot<DrawerConfig, DrawerContext>>

    fun setFullscreenDrawer(fullscreen: Boolean)

    sealed class Child {
        class Read(val component: ReadComponent) : Child()
        class Resources(val component: ResourcesComponent) : Child()
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit
) : MainComponent, KoinComponent, ComponentContext by componentContext {

    private val appStateStore: AppStateStore by inject()

    private val mainState = instanceKeeper.getOrCreate { MainStateKeeper() }

    private val _model = MutableValue(MainComponent.Model())
    override val model: Value<MainComponent.Model> = _model

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

    private val drawerNavigation = SlotNavigation<DrawerConfig>()

    override val drawerSlot: Value<ChildSlot<DrawerConfig, DrawerContext>> =
        childSlot(
            source = drawerNavigation,
            serializer = DrawerConfig.serializer(),
            handleBackButton = true,
            initialConfiguration = { null },
            childFactory = ::createDrawerComponent
        )

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
                    parentContext = this,
                    intent = config.intent,
                    sharedState = mainState,
                    onNavigateViewPhrase = ::onNavigateViewPhrase,
                    onNavigateEditPhrase = ::onNavigateEditPhrase
                )
            )
            is Config.Resources -> MainComponent.Child.Resources(
                DefaultResourcesComponent(
                    componentContext = context,
                    parentContext = this,
                    onShowSettingsDrawer = ::showSettingsDrawer
                )
            )
        }

    override fun onBackClick() {
        navigation.pop()
    }

    override fun openSettings() {
        showSettingsDrawer()
    }

    override fun openKeyTerms(book: Workbook, chapter: Chapter) {
        showKeyTermsDrawer(book, chapter)
    }

    fun showSettingsDrawer() {
        drawerNavigation.activate(DrawerConfig.Settings)
    }

    fun showKeyTermsDrawer(book: Workbook, chapter: Chapter) {
        val intent = KeyTermsIntent.Index(book, chapter)
        drawerNavigation.activate(DrawerConfig.KeyTerms(intent))
    }

    override fun dismissDrawer() {
        drawerNavigation.dismiss()
    }

    override fun setFullscreenDrawer(fullscreen: Boolean) {
        _model.update { it.copy(fullscreenDrawer = fullscreen) }
    }

    private fun onNavigateViewPhrase(phraseId: String) {
        val intent = KeyTermsIntent.ViewPhrase(phraseId)
        drawerNavigation.activate(DrawerConfig.KeyTerms(intent))
    }

    private fun onNavigateEditPhrase(phrase: String) {
        val intent = KeyTermsIntent.EditPhrase(phrase)
        drawerNavigation.activate(DrawerConfig.KeyTerms(intent))
    }

    private fun onNavigateBack() {
        val config = childStack.value.active.configuration
        when (config) {
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

    private fun selectActiveResource(resource: Resource) {
        _model.update { it.copy(activeResource = resource) }
        appStateStore.resourceStateHolder.updateResource(resource)
    }

    private fun selectActiveGlossary(glossary: Glossary) {
        _model.update { it.copy(activeGlossary = glossary) }
        appStateStore.glossaryStateHolder.updateGlossary(glossary)
    }

    private fun createDrawerComponent(
        config: DrawerConfig,
        context: ComponentContext
    ): DrawerContext {
        return when (config) {
            is DrawerConfig.Settings -> DefaultSettingsComponent(
                componentContext = context,
                parentContext = this,
                onSelectResource = ::selectActiveResource,
                onSelectGlossary = ::selectActiveGlossary,
                onFullscreen = ::setFullscreenDrawer
            )
            is DrawerConfig.KeyTerms -> DefaultKeyTermsComponent(
                componentContext = context,
                parentContext = this,
                intent = config.intent,
                sharedState = mainState,
                onFullscreen = ::setFullscreenDrawer
            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data class Read(val intent: ReadIntent = ReadIntent.Index) : Config
        @Serializable
        data object Resources : Config
    }
}
