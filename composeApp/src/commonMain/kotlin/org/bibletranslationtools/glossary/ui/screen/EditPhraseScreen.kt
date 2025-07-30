package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.cancel
import glossary.composeapp.generated.resources.description
import glossary.composeapp.generated.resources.editing_phrase
import glossary.composeapp.generated.resources.save_exit
import glossary.composeapp.generated.resources.saving
import glossary.composeapp.generated.resources.spelling
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseEvent
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseScreenModel
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class EditPhraseScreen(private val phrase: String) : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<EditPhraseScreenModel> {
            parametersOf(phrase)
        }
        val navigator = LocalNavigator.currentOrThrow
        val state by screenModel.state.collectAsStateWithLifecycle()

        var spelling by rememberSaveable {
            mutableStateOf("")
        }
        var description by rememberSaveable {
            mutableStateOf("")
        }

        val event by screenModel.event
            .collectAsStateWithLifecycle(EditPhraseEvent.Idle)

        LaunchedEffect(event) {
            when (event) {
                is EditPhraseEvent.OnPhraseSaved -> {
                    navigator.popUntil { it is TabbedScreen }
                }
                else -> {}
            }
        }

        LaunchedEffect(state.activePhrase) {
            state.activePhrase?.let { phrase ->
                if (spelling.isEmpty()) {
                    spelling = phrase.spelling
                }
                if (description.isEmpty()) {
                    description = phrase.description
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = stringResource(
                        Res.string.editing_phrase,
                        phrase
                    )
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
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(Res.string.spelling),
                        fontWeight = FontWeight.W600
                    )
                    OutlinedTextField(
                        value = spelling,
                        onValueChange = { spelling = it },
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        enabled = !state.isSaving,
                        shape = MaterialTheme.shapes.medium,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.8f
                            ),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.12f
                            ),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.1f
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.description),
                        fontWeight = FontWeight.W600
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        minLines = 5,
                        maxLines = 10,
                        enabled = !state.isSaving,
                        shape = MaterialTheme.shapes.medium,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.8f
                            ),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.12f
                            ),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.1f
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            enabled = spelling.isNotEmpty()
                                    && !state.isSaving,
                            onClick = {
                                screenModel.savePhrase(
                                    spelling = spelling,
                                    description = description
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.save_exit))
                        }
                        Button(
                            onClick = { navigator.popUntil { it is TabbedScreen } },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    state.error?.let { error ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    if (state.isSaving) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                            Text(stringResource(Res.string.saving))
                        }
                    }
                }
            }
        }
    }
}