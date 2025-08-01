package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_glossary
import glossary.composeapp.generated.resources.available_glossaries
import glossary.composeapp.generated.resources.save_exit
import org.bibletranslationtools.glossary.ui.components.GlossaryItem
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.event.AppEvent
import org.bibletranslationtools.glossary.ui.event.EventBus
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryListScreenModel
import org.jetbrains.compose.resources.stringResource

class GlossaryListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<GlossaryListScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        var isLoaded by remember { mutableStateOf(false) }
        val scrollState = rememberLazyListState()

        LaunchedEffect(state.selectedGlossary) {
            if (state.selectedGlossary != null && !isLoaded) {
                val index = state.glossaries.indexOf(state.selectedGlossary)
                scrollState.animateScrollToItem(index)
                isLoaded = true
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = stringResource(Res.string.available_glossaries)
                ) {
                    navigator.pop()
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 2.dp),
                        state = scrollState,
                        modifier = Modifier.heightIn(max = 412.dp)
                    ) {
                        items(state.glossaries) { item ->
                            GlossaryItem(
                                item = item,
                                isSelected = state.selectedGlossary == item,
                                onSelected = { screenModel.selectGlossary(item) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            state.selectedGlossary?.let { item ->
                                state.selectedResource?.let { resource ->
                                    EventBus.events.trySend(
                                        AppEvent.SelectResource(resource)
                                    )
                                    EventBus.events.trySend(
                                        AppEvent.SelectGlossary(item.glossary)
                                    )
                                    navigator.pop()
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.save_exit)
                        )
                    }

                    ElevatedButton(
                        onClick = {
                            navigator.push(ImportGlossaryScreen())
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.add_glossary)
                        )
                    }
                }
            }
        }
    }
}