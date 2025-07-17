package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_audio
import glossary.composeapp.generated.resources.edit
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.data.Resource
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.jetbrains.compose.resources.stringResource

class ViewPhraseScreen(
    private val phrase: Phrase,
    private val resource: Resource
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                BrowseTopBar(
                    title = phrase.phrase
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
                Text(phrase.spelling)
                Text(phrase.description)
                Row {
                    Button(onClick = {
                        navigator.push(EditPhraseScreen(phrase, resource))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                        Text(stringResource(Res.string.edit))
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Add Audio")
                        Text(stringResource(Res.string.add_audio))
                    }
                }
            }
        }
    }
}