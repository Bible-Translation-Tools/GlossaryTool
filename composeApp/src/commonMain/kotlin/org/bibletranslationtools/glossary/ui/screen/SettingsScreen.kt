package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.dark_mode
import glossary.composeapp.generated.resources.display
import glossary.composeapp.generated.resources.language
import glossary.composeapp.generated.resources.typography
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.ui.components.SettingsClickableItem
import org.bibletranslationtools.glossary.ui.components.SettingsSwitchItem
import org.jetbrains.compose.resources.stringResource

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        var theme by rememberStringSetting(
            Settings.THEME.name,
            Theme.SYSTEM.name
        )
        val isSystemInDarkTheme = isSystemInDarkTheme()
        var isDarkModeEnabled by remember {
            mutableStateOf(darkModeEnabled(theme, isSystemInDarkTheme))
        }

        LaunchedEffect(isDarkModeEnabled) {
            theme = if (isDarkModeEnabled) {
                Theme.DARK.name
            } else {
                Theme.LIGHT.name
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.display),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            SettingsClickableItem(
                                icon = Icons.Default.Translate,
                                text = stringResource(Res.string.language),
                                onClick = {}
                            )

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            SettingsClickableItem(
                                icon = Icons.Default.TextFields,
                                text = stringResource(Res.string.typography),
                                onClick = {}
                            )

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            SettingsSwitchItem(
                                icon = Icons.Default.DarkMode,
                                text = stringResource(Res.string.dark_mode),
                                checked = isDarkModeEnabled,
                                onCheckedChange = { isDarkModeEnabled = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun darkModeEnabled(
    theme: String,
    isSystemInDarkTheme: Boolean
): Boolean {
    return when {
        theme == Theme.SYSTEM.name && isSystemInDarkTheme -> true
        theme == Theme.LIGHT.name -> false
        theme == Theme.DARK.name -> true
        else -> false
    }
}