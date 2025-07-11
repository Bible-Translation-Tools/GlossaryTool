package org.bibletranslationtools.glossary.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar() {
    NavigationBar {
        // List of items for the bottom navigation
        val items = listOf(
            BottomNavItem("Read", Icons.Default.Book, "Read"),
            BottomNavItem("Glossary", Icons.AutoMirrored.Filled.List, "Glossary"),
            BottomNavItem("Resources", Icons.AutoMirrored.Filled.MenuBook, "Resources"),
            BottomNavItem("Settings", Icons.Default.Settings, "Settings")
        )

        // Currently selected item
        val selectedItem = items[0]

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedItem.route == item.route,
                onClick = { /* Handle navigation */ }
            )
        }
    }
}