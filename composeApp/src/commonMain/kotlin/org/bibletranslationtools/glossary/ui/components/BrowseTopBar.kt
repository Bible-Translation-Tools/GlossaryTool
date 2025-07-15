package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.browse
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTopBar(
    onBackClick: () -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.browse),
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* Handle search */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            Button(
                onClick = { /* Handle language change */ },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = "Language",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENG")
            }
        }
    )
}