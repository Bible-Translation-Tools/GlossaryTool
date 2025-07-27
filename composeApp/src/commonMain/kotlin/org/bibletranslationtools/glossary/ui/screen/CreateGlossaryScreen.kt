package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.new_glossary
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.jetbrains.compose.resources.stringResource

class CreateGlossaryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

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
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Create Glossary")
                }
            }
        }
    }
}