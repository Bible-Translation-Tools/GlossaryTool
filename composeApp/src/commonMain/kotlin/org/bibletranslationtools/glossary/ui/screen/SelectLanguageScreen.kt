package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.search_placeholder
import org.bibletranslationtools.glossary.data.Language
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.LanguageItem
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.screenmodel.SelectLanguageScreenModel
import org.jetbrains.compose.resources.stringResource

class SelectLanguageScreen(
    private val title: String,
    private val onSelect: (Language) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SelectLanguageScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        var filteredLanguages by remember(state.languages) {
            mutableStateOf(state.languages)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }

        LaunchedEffect(searchQuery) {
            filteredLanguages = if (searchQuery.isNotEmpty()) {
                state.languages.filter { language ->
                    language.slug.startsWith(searchQuery, ignoreCase = true)
                            || language.name.startsWith(searchQuery, ignoreCase = true)
                            || language.angName.startsWith(searchQuery, ignoreCase = true)
                }
            } else state.languages
        }

        Scaffold(
            topBar = {
                TopAppBar(title = title) {
                    navigator.pop()
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    SearchField(
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.search_placeholder),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        colors = CustomTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    HorizontalDivider()

                    LazyColumn {
                        items(filteredLanguages) { language ->
                            LanguageItem(
                                language = language,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigator.pop()
                                    onSelect(language)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}