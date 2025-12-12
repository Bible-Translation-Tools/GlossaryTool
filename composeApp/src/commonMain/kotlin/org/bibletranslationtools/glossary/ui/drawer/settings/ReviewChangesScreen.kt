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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import glossary.composeapp.generated.resources.pending_phrases
import glossary.composeapp.generated.resources.review_changes
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.api.PendingPhrase
import org.bibletranslationtools.glossary.data.api.UserRole
import org.bibletranslationtools.glossary.ui.components.PendingPhrase
import org.bibletranslationtools.glossary.ui.components.ReviewPendingPhraseBar
import org.bibletranslationtools.glossary.ui.components.SettingsSection
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ReviewChangesScreen(component: ReviewChangesComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val userState by appStateStore.userStateHolder.state
        .collectAsStateWithLifecycle()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()

    var selectedPhrase by remember { mutableStateOf<PendingPhrase?>(null) }
    val snackBar = LocalSnackBarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(glossaryState.glossary, userState.user) {
        glossaryState.glossary?.let { glossary ->
            component.loadPendingPhrases(glossary, false)
        }
    }

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
                        title = stringResource(Res.string.review_changes),
                        onBackClick = component::navigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PullToRefreshBox(
                        isRefreshing = model.isRefreshing,
                        onRefresh = {
                            glossaryState.glossary?.let { glossary ->
                                component.loadPendingPhrases(glossary, true)
                            }
                        }
                    ) {
                        SettingsSection(
                            title = stringResource(Res.string.pending_phrases)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.medium
                                    )
                            ) {
                                itemsIndexed(model.pendingPhrases) { index, pendingPhrase ->
                                    PendingPhrase(
                                        pendingPhrase = pendingPhrase,
                                        adminsCount = glossaryState.users.count {
                                            it.role == UserRole.OWNER || it.role == UserRole.ADMIN
                                        },
                                        onView = { selectedPhrase = pendingPhrase },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (index < model.pendingPhrases.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedPhrase?.let { pendingPhrase ->
        userState.user?.let { user ->
            ReviewPendingPhraseBar(
                pendingPhrase,
                me = user,
                onSave = {
                    component.saveReviewStatus(pendingPhrase, it)
                },
                onDismiss = {
                    selectedPhrase = null
                }
            )
        }
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