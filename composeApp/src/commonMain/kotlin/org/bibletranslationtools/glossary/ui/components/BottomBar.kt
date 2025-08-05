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
import org.bibletranslationtools.glossary.ui.main.MainComponent
import org.bibletranslationtools.glossary.ui.navigation.MainTab
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavBar(currentTab: MainComponent.Child, onTabSelected: (MainTab) -> Unit) {
    val tabs = MainTab.entries
    val activeColor = MaterialTheme.colorScheme.primary
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
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
            val isSelected = when (currentTab) {
                is MainComponent.Child.Glossary -> tab == MainTab.Glossary
                is MainComponent.Child.Read -> tab == MainTab.Read
                is MainComponent.Child.Resources -> tab == MainTab.Resources
                is MainComponent.Child.Settings -> tab == MainTab.Settings
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = stringResource(tab.title),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = stringResource(tab.title)
                    )
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
