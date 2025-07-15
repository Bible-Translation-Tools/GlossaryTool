package org.bibletranslationtools.glossary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.book_icon
import glossary.composeapp.generated.resources.glossary
import glossary.composeapp.generated.resources.glossary_icon
import glossary.composeapp.generated.resources.read
import glossary.composeapp.generated.resources.resources
import glossary.composeapp.generated.resources.resources_icon
import glossary.composeapp.generated.resources.settings
import glossary.composeapp.generated.resources.settings_icon
import org.bibletranslationtools.glossary.ui.screen.GlossaryScreen
import org.bibletranslationtools.glossary.ui.screen.ReadScreen
import org.bibletranslationtools.glossary.ui.screen.ResourcesScreen
import org.bibletranslationtools.glossary.ui.screen.SettingsScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

sealed interface MainTab : Tab {
    @Composable
    override fun Content()

    data object Read : MainTab {
        private fun readResolve(): Any = Read

        @Composable
        override fun Content() {
            Navigator(screen = ReadScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.read)
                val icon = painterResource(Res.drawable.book_icon)
                return remember { TabOptions(index = 0u, title = title, icon = icon) }
            }
    }

    data object Glossary : MainTab {
        private fun readResolve(): Any = Glossary

        @Composable
        override fun Content() {
            Navigator(screen = GlossaryScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.glossary)
                val icon = painterResource(Res.drawable.glossary_icon)
                return remember { TabOptions(index = 1u, title = title, icon = icon) }
            }
    }

    data object Resources : MainTab {
        private fun readResolve(): Any = Resources

        @Composable
        override fun Content() {
            Navigator(screen = ResourcesScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.resources)
                val icon = painterResource(Res.drawable.resources_icon)
                return remember { TabOptions(index = 2u, title = title, icon = icon) }
            }
    }

    data object Settings : MainTab {
        private fun readResolve(): Any = Settings

        @Composable
        override fun Content() {
            Navigator(screen = SettingsScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.settings)
                val icon = painterResource(Res.drawable.settings_icon)
                return remember { TabOptions(index = 3u, title = title, icon = icon) }
            }
    }
}