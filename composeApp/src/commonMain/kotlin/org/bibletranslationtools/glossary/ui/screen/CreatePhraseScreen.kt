package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.components.CustomTextFieldDefaults
import org.bibletranslationtools.glossary.ui.components.SearchField
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

class CreatePhraseScreen : Screen {

    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()

        val navigator = LocalNavigator.currentOrThrow

        var filteredPhrases by remember { mutableStateOf<List<String>>(emptyList()) }
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

        LaunchedEffect(searchQuery) {

        }

        Scaffold(
            topBar = {
                BrowseTopBar(
                    actions = {
                        SearchField(
                            searchQuery = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Start typing a word to search...",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.5f
                                    )
                                )
                            },
                            colors = CustomTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                            ),
                            modifier = Modifier.weight(1f)
                                .height(56.dp)
                        )
                    }
                ) {
                    navigator.popUntil { it is TabbedScreen }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                Text("Search for a word...")

                LazyColumn {
                    items(filteredPhrases) { phrase ->
                        Row {
                            Text(
                                text = phrase,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = "create"
                            )
                        }
                    }
                }
            }
        }

    }
}