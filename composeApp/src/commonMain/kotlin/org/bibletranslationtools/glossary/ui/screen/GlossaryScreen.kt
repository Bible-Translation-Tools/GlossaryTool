package org.bibletranslationtools.glossary.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.create_new_phrase
import glossary.composeapp.generated.resources.glossary_code
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.components.PhraseItem
import org.bibletranslationtools.glossary.ui.navigation.LocalAppState
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryEvent
import org.bibletranslationtools.glossary.ui.screenmodel.GlossaryScreenModel
import org.jetbrains.compose.resources.stringResource

class GlossaryScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<GlossaryScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val navigator = LocalRootNavigator.currentOrThrow
        val appState = LocalAppState.currentOrThrow

        var selectedGlossary by rememberStringSettingOrNull(
            Settings.GLOSSARY.name
        )

        LaunchedEffect(Unit) {
            selectedGlossary?.let {
                screenModel.onEvent(GlossaryEvent.LoadGlossary(it))
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
            ) {
                state.activeGlossary?.let { glossary ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(
                            Res.string.glossary_code,
                            glossary.code
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {}
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.create_new_phrase),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "add new word",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(glossary.phrases) { phrase ->
                            PhraseItem(phrase) {
                                navigator.push(ViewPhraseScreen(phrase))
                            }
                        }
                    }
                }

                if (state.activeGlossary == null && !state.isLoading) {
                    Text("Create Glossary")
                }
            }
        }
    }
}