package org.bibletranslationtools.glossary.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.bibletranslationtools.glossary.ui.navigation.MainTab

@Composable
fun BottomNavBar(currentTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    val tabs = listOf(MainTab.Read, MainTab.Glossary, MainTab.Resources, MainTab.Settings)
    val activeColor = MaterialTheme.colorScheme.primary
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.drawBehind {
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = borderColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab.options.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    tab.options.icon?.let { painter ->
                        Icon(painter = painter, contentDescription = tab.options.title)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawLine(
                        color = if (isSelected) activeColor else Color.Transparent,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                }
            )
        }
    }
}