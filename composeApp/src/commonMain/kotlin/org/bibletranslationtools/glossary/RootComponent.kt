package org.bibletranslationtools.glossary

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.ui.main.DefaultMainComponent
import org.bibletranslationtools.glossary.ui.main.MainComponent
import org.bibletranslationtools.glossary.ui.splash.DefaultSplashComponent
import org.bibletranslationtools.glossary.ui.splash.SplashComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Splash(val component: SplashComponent) : Child()
        class Main(val component: MainComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Splash,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Splash -> RootComponent.Child.Splash(
                DefaultSplashComponent(
                    componentContext = context,
                    onInitDone = {
                        navigation.replaceAll(Config.Main)
                    }
                )
            )
            is Config.Main -> RootComponent.Child.Main(
                DefaultMainComponent(context) {
                    onFinished()
                }
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Splash : Config

        @Serializable
        data object Main : Config
    }
}