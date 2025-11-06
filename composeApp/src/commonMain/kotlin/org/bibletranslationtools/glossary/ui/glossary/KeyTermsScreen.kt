package org.bibletranslationtools.glossary.ui.glossary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.create_new_phrase
import glossary.composeapp.generated.resources.export_glossary
import glossary.composeapp.generated.resources.glossary_code
import glossary.composeapp.generated.resources.key_terms
import glossary.composeapp.generated.resources.no_phrases_found
import glossary.composeapp.generated.resources.search
import glossary.composeapp.generated.resources.upload_glossary
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.PhraseItem
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val BOTTOM_BAR_HEIGHT = 80.dp

@Composable
fun KeyTermsScreen(component: KeyTermsComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val glossaryState by appStateStore.glossaryStateHolder.glossaryState
        .collectAsStateWithLifecycle()

    var filteredPhrases by remember(model.phrases) {
        mutableStateOf(model.phrases)
    }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(glossaryState.glossary) {
        glossaryState.glossary?.let {
            component.loadPhrases(it)
        } ?: run {
            component.navigateImportGlossary()
        }
    }

    LaunchedEffect(searchQuery) {
        filteredPhrases = model.phrases.filter { phrase ->
            phrase.phrase.contains(searchQuery, ignoreCase = true)
                    || phrase.spelling.contains(searchQuery, ignoreCase = true)
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
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.key_terms),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = component::dismiss
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close"
                            )
                        }
                    }

                    glossaryState.glossary?.let { glossary ->
                        Text(
                            text = stringResource(
                                Res.string.glossary_code,
                                glossary.code
                            ),
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        glossaryState.glossary?.let { glossary ->
                                            FileKit.openFileSaver(
                                                suggestedName = "glossary-${glossary.code}",
                                                extension = "zip"
                                            )?.let { file ->
                                                component.onExportGlossaryClicked(glossary, file)
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(Res.string.export_glossary)
                                )
                            }

                            TextButton(
                                onClick = {
                                    glossaryState.glossary?.let {
                                        component.onUploadGlossaryClicked(it)
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(Res.string.upload_glossary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

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
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            ),
                            modifier = Modifier.fillMaxWidth()
                                .height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredPhrases.isEmpty()) {
                            Text(text = stringResource(Res.string.no_phrases_found))
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = BOTTOM_BAR_HEIGHT
                            )
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
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.BottomCenter)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                text = stringResource(Res.string.create_new_phrase),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    model.progress?.let { progress ->
        ProgressDialog(progress)
    }
}