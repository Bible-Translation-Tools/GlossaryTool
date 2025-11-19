package org.bibletranslationtools.glossary.ui.drawer.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.active_users
import glossary.composeapp.generated.resources.edit_permissions
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.api.GlossaryUser
import org.bibletranslationtools.glossary.ui.components.EditPermissionsBar
import org.bibletranslationtools.glossary.ui.components.GlossaryUser
import org.bibletranslationtools.glossary.ui.components.SettingsSection
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun EditPermissionsScreen(component: EditPermissionsComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val userState by appStateStore.userStateHolder.state
        .collectAsStateWithLifecycle()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()

    var selectedUser by remember { mutableStateOf<GlossaryUser?>(null) }
    val snackBar = LocalSnackBarHostState.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    TopDrawerBar(
                        title = stringResource(Res.string.edit_permissions),
                        onBackClick = component::navigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSection(
                        title = stringResource(Res.string.active_users)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            itemsIndexed(glossaryState.users) { index, user ->
                                GlossaryUser(
                                    user = user,
                                    isOwner = user.username == userState.user?.username,
                                    onEdit = {
                                        selectedUser = user
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (index < glossaryState.users.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        EditPermissionsBar(
            user,
            onSave = { role ->
                if (user.role != role) {
                    glossaryState.glossary?.let { glossary ->
                        component.updateUserRole(glossary, user, role)
                    }
                }
            },
            onDismiss = {
                selectedUser = null
            }
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