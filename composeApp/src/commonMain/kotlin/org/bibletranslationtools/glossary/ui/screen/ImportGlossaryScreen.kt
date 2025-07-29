package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.navigation.LocalRootNavigator
import org.bibletranslationtools.glossary.ui.screenmodel.ImportGlossaryScreenModel
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.koin.compose.koinInject

class ImportGlossaryScreen : Screen {

    @Composable
    override fun Content() {
        val appStateStore = koinInject<AppStateStore>()
        val navigator = LocalRootNavigator.currentOrThrow

        val screenModel = koinScreenModel<ImportGlossaryScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()

        val glossaryState by appStateStore.glossaryStateHolder.glossaryState
            .collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = "Add Glossary"
                ) {
                    navigator.pop()
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Download Glossary. Coming soon..."
                    )
                }
            }
        }
    }
}