package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
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
import glossary.composeapp.generated.resources.spelling
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseEvent
import org.bibletranslationtools.glossary.ui.screenmodel.EditPhraseScreenModel
import org.bibletranslationtools.glossary.ui.screenmodel.HomeEvent
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class EditPhraseScreen(
    private val phrase: Phrase,
    private val resource: Resource
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<EditPhraseScreenModel> {
            parametersOf(phrase, resource)
        }
        val navigator = LocalNavigator.currentOrThrow

        var spelling by remember {
            mutableStateOf(TextFieldValue(phrase.spelling))
        }
        var description by remember {
            mutableStateOf(TextFieldValue(phrase.description))
        }

        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        LaunchedEffect(event) {
            when (event) {
                is EditPhraseEvent.OnPhraseSaved -> {
                    navigator.popUntil { it is ReadScreen }
                }
                else -> {}
            }
        }

        Scaffold(
            topBar = {
                BrowseTopBar(
                    title = stringResource(
                        Res.string.editing_phrase,
                        phrase.phrase
                    )
                ) {
                    navigator.popUntil { it is ReadScreen }
                }
            }
        ) { paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
            ) {
                TextField(
                    value = spelling,
                    onValueChange = { spelling = it },
                    label = { Text(stringResource(Res.string.spelling)) },
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.description)) },
                    maxLines = 5
                )

                Column {
                    Button(
                        enabled = spelling.text.isNotEmpty() && description.text.isNotEmpty(),
                        onClick = {
                            viewModel.onEvent(
                                EditPhraseEvent.SavePhrase(
                                    spelling.text,
                                    description.text
                                )
                            )
                        }
                    ) {
                        Text(stringResource(Res.string.save_exit))
                    }
                    Button(onClick = { navigator.pop() }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}