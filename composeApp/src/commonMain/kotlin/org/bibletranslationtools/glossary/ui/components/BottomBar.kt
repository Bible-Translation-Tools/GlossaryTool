package org.bibletranslationtools.glossary.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavBar(currentTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    val tabs = listOf(MainTab.Read, MainTab.Glossary, MainTab.Resources, MainTab.Settings)

    NavigationBar {
        tabs.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.options.title) },
                icon = {
                    tab.options.icon?.let { painter ->
                        Icon(painter = painter, contentDescription = tab.options.title)
                    }
                }
            )
        }
    }
}