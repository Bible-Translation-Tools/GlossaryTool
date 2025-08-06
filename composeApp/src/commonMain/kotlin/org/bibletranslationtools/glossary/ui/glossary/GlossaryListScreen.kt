package org.bibletranslationtools.glossary.ui.glossary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.add_glossary
import glossary.composeapp.generated.resources.available_glossaries
import glossary.composeapp.generated.resources.save_exit
import org.bibletranslationtools.glossary.ui.components.GlossaryItem
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlossaryListScreen(component: GlossaryListComponent) {
    val model by component.model.subscribeAsState()

    var isLoaded by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        component.setTopAppBar {
            TopAppBar(
                title = stringResource(Res.string.available_glossaries)
            ) {
                component.navigateBack()
            }
        }
    }

    LaunchedEffect(model.selectedGlossary) {
        if (model.selectedGlossary != null && !isLoaded) {
            val index = model.glossaries.indexOf(model.selectedGlossary)
            scrollState.animateScrollToItem(index)
            isLoaded = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
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
                        onSelected = { component.selectGlossary(item) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    component.saveGlossary()
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(Res.string.save_exit)
                )
            }

            ElevatedButton(
                onClick = {
                    component.navigateImportGlossary()
                },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(Res.string.add_glossary)
                )
            }
        }
    }
}