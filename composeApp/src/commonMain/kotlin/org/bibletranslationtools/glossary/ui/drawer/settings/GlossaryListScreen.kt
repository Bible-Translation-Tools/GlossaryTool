package org.bibletranslationtools.glossary.ui.drawer.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_create_glossary
import glossary.composeapp.generated.resources.add_glossary
import glossary.composeapp.generated.resources.available_glossaries
import glossary.composeapp.generated.resources.create_glossary
import glossary.composeapp.generated.resources.glossaries_unavailable
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.ui.components.GlossaryItem
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.bibletranslationtools.glossary.ui.dialogs.ProgressDialog
import org.bibletranslationtools.glossary.ui.navigation.LocalSnackBarHostState
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlossaryListScreen(component: GlossaryListComponent) {
    val model by component.model.subscribeAsState()

    var isLoaded by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val snackBar = LocalSnackBarHostState.current

    LaunchedEffect(model.selectedGlossary) {
        if (model.selectedGlossary != null && !isLoaded) {
            val index = model.glossaries.indexOf(model.selectedGlossary)
            scrollState.animateScrollToItem(index)
            isLoaded = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = stringResource(Res.string.available_glossaries)
        ) {
            component.navigateBack()
        }

        Column(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (model.glossaries.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 2.dp),
                            state = scrollState,
                            modifier = Modifier.heightIn(max = 412.dp)
                        ) {
                            items(model.glossaries) { item ->
                                GlossaryItem(
                                    item = item,
                                    isSelected = model.selectedGlossary == item,
                                    isActive = model.activeGlossary == item,
                                    onSelected = { component.selectGlossary(item) },
                                    onSelectedSave = component::saveGlossary,
                                    onShare = component::uploadGlossary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                space = 8.dp,
                                alignment = Alignment.CenterVertically
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.glossaries_unavailable),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )

                            Text(
                                text = stringResource(Res.string.add_create_glossary),
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = component::navigateImportGlossary,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(stringResource(Res.string.add_glossary))
                            }

                            ElevatedButton(
                                onClick = component::navigateCreateGlossary,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(stringResource(Res.string.create_glossary))
                            }
                        }
                    }
                }
            }
        }
    }

    model.progress?.let { progress ->
        ProgressDialog(progress)
    }

    model.snackBarMessage?.let { message ->
        coroutineScope.launch {
            component.clearSnackBarMessage()
            snackBar?.showSnackbar(message)
        }
    }
}
