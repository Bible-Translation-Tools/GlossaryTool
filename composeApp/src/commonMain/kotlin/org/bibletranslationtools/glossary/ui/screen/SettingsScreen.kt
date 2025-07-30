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
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.DarkMode
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.admin
import glossary.composeapp.generated.resources.dark_mode
import glossary.composeapp.generated.resources.developer_guide
import glossary.composeapp.generated.resources.display
import glossary.composeapp.generated.resources.edit_rules
import glossary.composeapp.generated.resources.format_list_bulleted_add
import glossary.composeapp.generated.resources.language
import glossary.composeapp.generated.resources.new_glossary
import glossary.composeapp.generated.resources.typography
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.ui.components.SettingsClickableItem
import org.bibletranslationtools.glossary.ui.components.SettingsSwitchItem
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalRootNavigator.currentOrThrow

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
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\uD83D\uDE00",
                        fontSize = 40.sp
                    )
                    Text(
                        text = "User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "user@mail.net",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.admin),
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
                                icon = painterResource(Res.drawable.format_list_bulleted_add),
                                text = stringResource(Res.string.new_glossary),
                                onClick = {
                                    navigator.push(CreateGlossaryScreen())
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            SettingsClickableItem(
                                icon = painterResource(Res.drawable.developer_guide),
                                text = stringResource(Res.string.edit_rules),
                                onClick = {}
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                                icon = Icons.Outlined.DarkMode,
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