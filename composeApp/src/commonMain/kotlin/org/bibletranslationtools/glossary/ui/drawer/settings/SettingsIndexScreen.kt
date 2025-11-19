package org.bibletranslationtools.glossary.ui.drawer.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.about_app
import glossary.composeapp.generated.resources.check_updates
import glossary.composeapp.generated.resources.dark_mode
import glossary.composeapp.generated.resources.data_privacy
import glossary.composeapp.generated.resources.edit_account
import glossary.composeapp.generated.resources.edit_permissions
import glossary.composeapp.generated.resources.format_list_bulleted
import glossary.composeapp.generated.resources.format_list_bulleted_add
import glossary.composeapp.generated.resources.interface_settings
import glossary.composeapp.generated.resources.language
import glossary.composeapp.generated.resources.login_wacs
import glossary.composeapp.generated.resources.new_glossary
import glossary.composeapp.generated.resources.other_settings
import glossary.composeapp.generated.resources.person_edit
import glossary.composeapp.generated.resources.review_changes
import glossary.composeapp.generated.resources.search_check
import glossary.composeapp.generated.resources.settings
import glossary.composeapp.generated.resources.shield_lock
import glossary.composeapp.generated.resources.source_text_settings
import glossary.composeapp.generated.resources.terms_and_conditions
import glossary.composeapp.generated.resources.user_settings
import glossary.composeapp.generated.resources.view_glossaries
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.api.UserRole
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.domain.Theme
import org.bibletranslationtools.glossary.ui.components.SegmentedButtonRow
import org.bibletranslationtools.glossary.ui.components.SettingsClickableItem
import org.bibletranslationtools.glossary.ui.components.SettingsSection
import org.bibletranslationtools.glossary.ui.components.SettingsSwitchItem
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar
import org.bibletranslationtools.glossary.ui.dialogs.LoginDialog
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SettingsIndexScreen(component: SettingsIndexComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val userState by appStateStore.userStateHolder.state
        .collectAsStateWithLifecycle()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()

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

    val scrollState = rememberScrollState()
    val snackBar = LocalSnackBarHostState.current
    val scope = rememberCoroutineScope()

    var showLoginDialog by remember { mutableStateOf(false) }
    var emoji by remember { mutableStateOf(userState.user?.emoji) }

    val isAdmin by remember(glossaryState.users) {
        mutableStateOf(
            glossaryState.users
                .filter { it.role == UserRole.OWNER || it.role == UserRole.ADMIN }
                .map { it.username }
                .contains(userState.user?.username)
        )
    }

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
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        userState.user?.let { user ->
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = user.emoji,
                                    fontSize = 40.sp
                                )
                                Text(
                                    text = user.username,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        component.logout()
                                    }
                                )
                            }
                        } ?: run {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(
                                    onClick = { showLoginDialog = true }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.login_wacs),
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            }

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
                            if (isAdmin) {
                                SettingsClickableItem(
                                    icon = painterResource(Res.drawable.person_edit),
                                    text = stringResource(Res.string.edit_account),
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth()
                                )
                                SettingsClickableItem(
                                    icon = painterResource(Res.drawable.person_edit),
                                    text = stringResource(Res.string.edit_permissions),
                                    onClick = component::editPermissions,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            SettingsClickableItem(
                                icon = painterResource(Res.drawable.format_list_bulleted_add),
                                text = stringResource(Res.string.new_glossary),
                                onClick = component::createGlossary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            SettingsClickableItem(
                                icon = painterResource(Res.drawable.format_list_bulleted),
                                text = stringResource(Res.string.view_glossaries),
                                onClick = component::viewGlossaries
                            )
                            SettingsClickableItem(
                                icon = painterResource(Res.drawable.search_check),
                                text = stringResource(Res.string.review_changes),
                                actionText = "(31)",
                                onClick = {}
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsSection(
                            title = stringResource(Res.string.other_settings)
                        ) {
                            SettingsClickableItem(
                                icon = Icons.Default.Refresh,
                                text = stringResource(Res.string.check_updates),
                                onClick = component::checkUpdates
                            )
                            SettingsClickableItem(
                                icon = Icons.Default.Info,
                                text = stringResource(Res.string.about_app),
                                onClick = {}
                            )
                            SettingsClickableItem(
                                icon = Icons.Outlined.Description,
                                text = stringResource(Res.string.terms_and_conditions),
                                onClick = {}
                            )
                            SettingsClickableItem(
                                icon = painterResource(Res.drawable.shield_lock),
                                text = stringResource(Res.string.data_privacy),
                                onClick = {}
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        LoginDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = component::login
        )
    }

    model.progress?.let { progress ->
        ProgressDialog(progress)
    }

    model.snackBarMessage?.let { message ->
        scope.launch {
            component.clearSnackBarMessage()
            snackBar?.showSnackbar(message)
        }
    }
}

private fun darkModeEnabled(
    theme: String,
    isSystemInDarkTheme: Boolean
): Boolean {
    return when (theme) {
        Theme.SYSTEM if isSystemInDarkTheme -> true
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> false
    }
}