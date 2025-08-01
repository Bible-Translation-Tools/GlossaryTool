package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.cancel
import glossary.composeapp.generated.resources.create_glossary
import glossary.composeapp.generated.resources.dictionary
import glossary.composeapp.generated.resources.download
import glossary.composeapp.generated.resources.download_resource_request
import glossary.composeapp.generated.resources.glossary_code
import glossary.composeapp.generated.resources.new_glossary
import glossary.composeapp.generated.resources.ok
import glossary.composeapp.generated.resources.saving
import glossary.composeapp.generated.resources.share_code_hint
import glossary.composeapp.generated.resources.source_language
import glossary.composeapp.generated.resources.target_language
import org.bibletranslationtools.glossary.Utils
import org.bibletranslationtools.glossary.ui.components.LanguageSelector
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.dialogs.ConfirmDialog
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.event.AppEvent
import org.bibletranslationtools.glossary.ui.event.EventBus
import org.bibletranslationtools.glossary.ui.screenmodel.CreateGlossaryEvent
import org.bibletranslationtools.glossary.ui.screenmodel.CreateGlossaryScreenModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class CreateGlossaryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<CreateGlossaryScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val event by screenModel.event
            .collectAsStateWithLifecycle(CreateGlossaryEvent.Idle)

        val code by rememberSaveable {
            mutableStateOf(Utils.randomCode())
        }

        val createEnabled by remember {
            derivedStateOf {
                state.sourceLanguage != null && state.targetLanguage != null
            }
        }

        val sourceLangText = stringResource(Res.string.source_language)
        val targetLangText = stringResource(Res.string.target_language)

        LaunchedEffect(event) {
            when (event) {
                is CreateGlossaryEvent.OnGlossaryCreated -> {
                    val glossary = (event as CreateGlossaryEvent.OnGlossaryCreated).glossary
                    EventBus.events.send(AppEvent.SelectGlossary(glossary))
                    navigator.popUntil { it is TabbedScreen }
                }
                is CreateGlossaryEvent.OnResourceDownloaded -> {
                    val resource = (event as CreateGlossaryEvent.OnResourceDownloaded).resource
                    EventBus.events.send(AppEvent.SelectResource(resource))
                    screenModel.createGlossary(code)
                }
                else -> {}
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = stringResource(Res.string.new_glossary)
                ) {
                    navigator.popUntil { it is TabbedScreen }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(Res.drawable.dictionary),
                        contentDescription = "dictionary",
                        modifier = Modifier.size(120.dp)
                    )
                    Text(
                        text = stringResource(
                            Res.string.glossary_code,
                            code
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Text(
                        text = stringResource(Res.string.share_code_hint),
                        textAlign = TextAlign.Center
                    )

                    LanguageSelector(
                        title = stringResource(Res.string.source_language),
                        language = state.sourceLanguage,
                        onClick = {
                            navigator.push(
                                SelectLanguageScreen(
                                    title = sourceLangText,
                                    onSelect = { language ->
                                        screenModel.setSourceLanguage(language)
                                    }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    LanguageSelector(
                        title = stringResource(Res.string.target_language),
                        language = state.targetLanguage,
                        onClick = {
                            navigator.push(
                                SelectLanguageScreen(
                                    title = targetLangText,
                                    onSelect = { language ->
                                        screenModel.setTargetLanguage(language)
                                    }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            screenModel.createGlossary(code)
                        },
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = if (createEnabled) {
                                Color.Unspecified
                            } else {
                                Color.Transparent
                            }
                        ),
                        enabled = createEnabled,
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.create_glossary),
                            fontSize = 16.sp
                        )
                    }

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

                    state.resourceRequest?.let { request ->
                        ConfirmDialog(
                            title = stringResource(Res.string.download),
                            text = stringResource(Res.string.download_resource_request),
                            confirmButtonText = stringResource(Res.string.ok),
                            dismissButtonText = stringResource(Res.string.cancel),
                            onConfirm = {
                                screenModel.downloadResource(request)
                            },
                            onDismiss = {
                                screenModel.clearResourceRequest()
                            }
                        )
                    }

                    state.progress?.let { progress ->
                        ProgressDialog(progress)
                    }
                }
            }
        }
    }
}