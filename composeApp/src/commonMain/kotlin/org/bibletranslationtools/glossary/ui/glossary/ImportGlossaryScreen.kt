package org.bibletranslationtools.glossary.ui.glossary

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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.bibletranslationtools.glossary.ui.components.TopAppBar

@Composable
fun ImportGlossaryScreen(component: ImportGlossaryComponent) {
    val model by component.model.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Add Glossary"
            ) {
                component.onBackClicked()
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