package org.bibletranslationtools.glossary.ui.glossary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_audio
import glossary.composeapp.generated.resources.edit
import org.bibletranslationtools.glossary.data.toOption
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.components.VerseReference
import org.bibletranslationtools.glossary.ui.state.AppStateStore
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ViewPhraseScreen(component: ViewPhraseComponent) {
    val model by component.model.subscribeAsState()

    val appStateStore = koinInject<AppStateStore>()
    val resourceState by appStateStore.resourceStateHolder.resourceState
        .collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        component.setTopAppBar {
            TopAppBar(
                title = model.phrase?.phrase,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                component.onBackClick()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.phrase?.spelling ?: "",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!model.phrase?.audio.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(model.phrase?.description ?: "")

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        model.phrase?.let { phrase ->
                            component.onEditClick(phrase.phrase)
                        }
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
                    enabled = false,
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

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .weight(1f)
        ) {
            if (model.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    items(model.refs) { ref ->
                        resourceState.resource?.let { resource ->
                            val reference = "${ref.book.uppercase()} ${ref.chapter}:${ref.verse}"
                            val text = ref.getText(resource)

                            model.phrase?.let { phrase ->
                                VerseReference(
                                    reference = reference,
                                    phrase = phrase.phrase,
                                    text = text,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    component.onRefClick(ref.toOption())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
