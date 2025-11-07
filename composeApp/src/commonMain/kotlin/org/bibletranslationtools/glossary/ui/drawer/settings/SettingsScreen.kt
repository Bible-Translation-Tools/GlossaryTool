package org.bibletranslationtools.glossary.ui.drawer.settings

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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.dark_mode
import glossary.composeapp.generated.resources.developer_guide
import glossary.composeapp.generated.resources.edit_rules
import glossary.composeapp.generated.resources.format_list_bulleted_add
import glossary.composeapp.generated.resources.interface_settings
import glossary.composeapp.generated.resources.language
import glossary.composeapp.generated.resources.new_glossary
import glossary.composeapp.generated.resources.settings
import glossary.composeapp.generated.resources.source_text_settings
import glossary.composeapp.generated.resources.user_settings
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.ui.components.SegmentedButtonRow
import org.bibletranslationtools.glossary.ui.components.SettingsClickableItem
import org.bibletranslationtools.glossary.ui.components.SettingsSection
import org.bibletranslationtools.glossary.ui.components.SettingsSwitchItem
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(component: SettingsComponent) {

    var theme by rememberStringSetting(
        Settings.THEME,
        Theme.SYSTEM
    )
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDarkModeEnabled by remember {
        mutableStateOf(darkModeEnabled(theme, isSystemInDarkTheme))
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedIndex2 by remember { mutableIntStateOf(0) }
    var selectedIndex3 by remember { mutableIntStateOf(0) }

    LaunchedEffect(isDarkModeEnabled) {
        theme = if (isDarkModeEnabled) {
            Theme.DARK
        } else {
            Theme.LIGHT
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    TopDrawerBar(
                        title = stringResource(Res.string.settings),
                        onDismiss = component::dismiss,
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    SettingsSection(
                        title = stringResource(Res.string.source_text_settings)
                    ) {
                        SegmentedButtonRow(
                            options = listOf("Aa", "Aa", "Aa"),
                            selectedIndex = selectedIndex,
                            onSelectionChange = { selectedIndex = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SegmentedButtonRow(
                            options = listOf("Small", "Medium", "Large"),
                            selectedIndex = selectedIndex2,
                            onSelectionChange = { selectedIndex2 = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SegmentedButtonRow(
                            options = listOf("Small", "Default", "Large"),
                            selectedIndex = selectedIndex3,
                            onSelectionChange = { selectedIndex3 = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSection(
                        title = stringResource(Res.string.interface_settings)
                    ) {
                        SettingsClickableItem(
                            icon = Icons.Default.Translate,
                            text = stringResource(Res.string.language),
                            actionText = "English",
                            onClick = {}
                        )
                        SettingsSwitchItem(
                            icon = Icons.Outlined.DarkMode,
                            text = stringResource(Res.string.dark_mode),
                            checked = isDarkModeEnabled,
                            onCheckedChange = { isDarkModeEnabled = it }
                        )
                    }

                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSection(
                        title = stringResource(Res.string.user_settings)
                    ) {
                        SettingsClickableItem(
                            icon = painterResource(Res.drawable.format_list_bulleted_add),
                            text = stringResource(Res.string.new_glossary),
                            onClick = {
                                component.createGlossary()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SettingsClickableItem(
                            icon = painterResource(Res.drawable.developer_guide),
                            text = stringResource(Res.string.edit_rules),
                            onClick = {}
                        )
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
        theme == Theme.SYSTEM && isSystemInDarkTheme -> true
        theme == Theme.LIGHT -> false
        theme == Theme.DARK -> true
        else -> false
    }
}