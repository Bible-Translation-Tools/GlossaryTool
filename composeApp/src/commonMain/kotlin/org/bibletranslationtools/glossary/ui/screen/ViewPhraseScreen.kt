package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_audio
import glossary.composeapp.generated.resources.edit
import org.bibletranslationtools.glossary.ui.components.BrowseTopBar
import org.bibletranslationtools.glossary.ui.screenmodel.PhraseDetails
import org.jetbrains.compose.resources.stringResource

class ViewPhraseScreen(
    private val phraseDetails: PhraseDetails
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                BrowseTopBar(
                    title = phraseDetails.phrase.phrase
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phraseDetails.phrase.spelling,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(phraseDetails.phrase.description)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                navigator.push(EditPhraseScreen(phraseDetails))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(0.48f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.edit))
                        }
                        Spacer(modifier = Modifier.weight(0.04f))
                        Button(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(0.48f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Add Audio",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(stringResource(Res.string.add_audio))
                        }
                    }
                }
            }
        }
    }
}