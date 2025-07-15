package org.bibletranslationtools.glossary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.glossary
import glossary.composeapp.generated.resources.read
import glossary.composeapp.generated.resources.resources
import glossary.composeapp.generated.resources.settings
import org.bibletranslationtools.glossary.ui.screen.GlossaryScreen
import org.bibletranslationtools.glossary.ui.screen.ReadScreen
import org.bibletranslationtools.glossary.ui.screen.ResourcesScreen
import org.bibletranslationtools.glossary.ui.screen.SettingsScreen
import org.jetbrains.compose.resources.stringResource

sealed interface MainTab : Tab {
    @Composable
    override fun Content()

    data object Read : MainTab {
        @Composable
        override fun Content() {
            Navigator(screen = ReadScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.read)
                val icon = rememberVectorPainter(Icons.Default.Book)
                return remember { TabOptions(index = 0u, title = title, icon = icon) }
            }
    }

    data object Glossary : MainTab {
        @Composable
        override fun Content() {
            Navigator(screen = GlossaryScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.glossary)
                val icon = rememberVectorPainter(Icons.AutoMirrored.Filled.List)
                return remember { TabOptions(index = 1u, title = title, icon = icon) }
            }
    }

    data object Resources : MainTab {
        @Composable
        override fun Content() {
            Navigator(screen = ResourcesScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.resources)
                val icon = rememberVectorPainter(Icons.AutoMirrored.Filled.MenuBook)
                return remember { TabOptions(index = 2u, title = title, icon = icon) }
            }
    }

    data object Settings : MainTab {
        @Composable
        override fun Content() {
            Navigator(screen = SettingsScreen())
        }

        override val options: TabOptions
            @Composable
            get() {
                val title = stringResource(Res.string.settings)
                val icon = rememberVectorPainter(Icons.Default.Settings)
                return remember { TabOptions(index = 3u, title = title, icon = icon) }
            }
    }
}