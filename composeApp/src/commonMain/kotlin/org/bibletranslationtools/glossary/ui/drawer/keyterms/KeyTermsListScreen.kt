package org.bibletranslationtools.glossary.ui.drawer.keyterms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_glossary
import glossary.composeapp.generated.resources.add_glossary_key_terms
import glossary.composeapp.generated.resources.check_updates
import glossary.composeapp.generated.resources.create_glossary
import glossary.composeapp.generated.resources.create_new_phrase
import glossary.composeapp.generated.resources.glossary_code
import glossary.composeapp.generated.resources.join_glossary
import glossary.composeapp.generated.resources.key_terms
import glossary.composeapp.generated.resources.key_terms_unavailable
import glossary.composeapp.generated.resources.no_phrases_found
import glossary.composeapp.generated.resources.search
import glossary.composeapp.generated.resources.upload_glossary
import glossary.composeapp.generated.resources.upload_phrases
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.data.api.UserRole
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.GlossaryUpdate
import org.bibletranslationtools.glossary.ui.components.PhraseItem
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.components.TopDrawerBar
import org.bibletranslationtools.glossary.ui.components.UpdateStatus
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun KeyTermsListScreen(component: KeyTermsListComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val glossaryState by appStateStore.glossaryStateHolder.state
        .collectAsStateWithLifecycle()
    val userState by appStateStore.userStateHolder.state
        .collectAsStateWithLifecycle()

    var currentPhrases by remember {
        mutableStateOf(model.chapterPhrases)
    }

    var filteredPhrases by remember {
        mutableStateOf(currentPhrases)
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedOption by rememberSaveable { mutableIntStateOf(0) }

    val selectedColor = MaterialTheme.colorScheme.primary
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val unselectedColor = MaterialTheme.colorScheme.outlineVariant

    var activeBookSlug by rememberStringSetting(
        Settings.BOOK,
        "mat"
    )
    var activeChapterNum by rememberIntSetting(
        Settings.CHAPTER,
        1
    )

    var glossaryUpdateStatus by remember(model.updateStatus) {
        mutableStateOf(model.updateStatus)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBar = LocalSnackBarHostState.current

    var joined by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var canEdit by remember { mutableStateOf(false) }
    var isGlossaryPublished by remember { mutableStateOf(false) }

    LaunchedEffect(glossaryState.glossary) {
        glossaryState.glossary?.let { glossary ->
            component.initialize(
                glossary,
                activeBookSlug,
                activeChapterNum
            )
        }
    }

    LaunchedEffect(glossaryState.users, userState.user) {
        userState.user?.let { glossaryUser ->
            joined = glossaryState.users
                .map { it.user.username }
                .contains(glossaryUser.username)

            canEdit = glossaryState.users
                .filter { it.role != UserRole.VIEWER }
                .map { it.user.username }
                .contains(glossaryUser.username)

            isAdmin = glossaryState.users
                .filter { it.role == UserRole.OWNER || it.role == UserRole.ADMIN }
                .map { it.user.username }
                .contains(glossaryUser.username)

            isGlossaryPublished = glossaryState.users.firstOrNull()?.published ?: false
        }
    }

    LaunchedEffect(searchQuery, currentPhrases) {
        filteredPhrases = currentPhrases.filter { phrase ->
            phrase.phrase.contains(searchQuery, ignoreCase = true)
                    || phrase.spelling.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(
        selectedOption,
        model.filterOptions,
        model.allPhrases,
        model.chapterPhrases
    ) {
        if (model.filterOptions.isNotEmpty()) {
            val option = model.filterOptions[selectedOption]
            currentPhrases = when (option) {
                is KeyTermsFilter.Chapter -> model.chapterPhrases
                is KeyTermsFilter.SourceText -> model.allPhrases
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    TopDrawerBar(
                        title = stringResource(Res.string.key_terms),
                        subTitle = glossaryState.glossary?.let {
                            stringResource(
                                Res.string.glossary_code,
                                it.code
                            )
                        },
                        onDismiss = component::dismiss,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!joined && model.glossary != null && userState.user != null) {
                        Text(
                            text = stringResource(Res.string.join_glossary),
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable {
                                component.joinGlossary()
                            }
                        )
                    }

                    model.glossary?.let {
                        Spacer(modifier = Modifier.height(32.dp))

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            model.filterOptions.forEachIndexed { index, option ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = model.filterOptions.size,
                                        baseShape = MaterialTheme.shapes.medium
                                    ),
                                    onClick = { selectedOption = index },
                                    selected = selectedOption == index,
                                    label = {
                                        Text(
                                            text = option.label,
                                            fontSize = 16.sp,
                                            fontWeight = if (selectedOption == index) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            color = if (selectedOption == index) {
                                                selectedColor
                                            } else Color.Unspecified
                                        )
                                    },
                                    border = SegmentedButtonDefaults.borderStroke(
                                        color = if (selectedOption == index) {
                                            selectedColor
                                        } else unselectedColor
                                    ),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = if (selectedOption == index) {
                                            selectedContainerColor
                                        } else Color.Unspecified,
                                    ),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    icon = {}
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SearchField(
                            searchQuery = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.search),
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f
                                    )
                                )
                            },
                            colors = CustomTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground
                                    .copy(alpha = 0.1f),
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (model.isLoading) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .weight(1f)
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (filteredPhrases.isEmpty()) {
                                Text(text = stringResource(Res.string.no_phrases_found))
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredPhrases) { phrase ->
                                    PhraseItem(
                                        phrase = phrase,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            phrase.id?.let(component::navigateViewPhrase)
                                        }
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )

                                if (model.glossary?.hasUpdate == true
                                    || glossaryUpdateStatus != UpdateStatus.DEFAULT) {
                                    GlossaryUpdate(
                                        status = glossaryUpdateStatus,
                                        onDownload = component::downloadGlossary,
                                        onDismiss = component::clearHasUpdate
                                    )
                                } else {
                                    Column (
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        if (canEdit) {
                                            Button(
                                                onClick = component::navigateSearchPhrases,
                                                shape = MaterialTheme.shapes.medium,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "add new word",
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(
                                                        Res.string.create_new_phrase
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }

                                            when {
                                                isAdmin && !isGlossaryPublished -> {
                                                    // Upload glossary
                                                    ElevatedButton(
                                                        onClick = component::updateGlossary,
                                                        shape = MaterialTheme.shapes.medium,
                                                        colors = ButtonDefaults.elevatedButtonColors(
                                                            containerColor =
                                                                MaterialTheme.colorScheme.primaryContainer
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                            .height(40.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(
                                                                Res.string.upload_glossary
                                                            )
                                                        )
                                                    }
                                                }
                                                isGlossaryPublished && model.allPhrases.any {
                                                    it.pending
                                                } -> {
                                                    // Upload pending phrases
                                                    ElevatedButton(
                                                        onClick = component::uploadPendingPhrases,
                                                        shape = MaterialTheme.shapes.medium,
                                                        colors = ButtonDefaults.elevatedButtonColors(
                                                            containerColor =
                                                                MaterialTheme.colorScheme.primaryContainer
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                            .height(40.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(
                                                                Res.string.upload_phrases
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = component::checkForUpdates,
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    Res.string.check_updates
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                space = 8.dp,
                                alignment = Alignment.CenterVertically
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.key_terms_unavailable),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )

                            Text(
                                text = stringResource(Res.string.add_glossary_key_terms),
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = component::navigateImportGlossary,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(stringResource(Res.string.add_glossary))
                            }

                            ElevatedButton(
                                onClick = component::navigateCreateGlossary,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(stringResource(Res.string.create_glossary))
                            }
                        }
                    }
                }
            }
        }
    }

    model.progress?.let { progress ->
        ProgressDialog(progress)
    }

    model.snackBarMessage?.let { message ->
        coroutineScope.launch {
            component.clearSnackBarMessage()
            snackBar?.showSnackbar(message)
        }
    }
}