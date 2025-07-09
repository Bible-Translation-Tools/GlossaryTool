package org.bibletranslationtools.glossary.ui.screen

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import kotlinx.datetime.Clock
import org.bibletranslationtools.glossary.persistence.GlossaryDataSource
import org.bibletranslationtools.glossary.ui.viewmodel.HomeViewModel
import org.koin.compose.koinInject

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeViewModel>()

        val navigator = LocalNavigator.currentOrThrow
        val coroutine = rememberCoroutineScope()

        Scaffold {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    Text("Home")
                    Button(onClick = { viewModel.insert() }) {
                        Text("Insert")
                    }
                    Button(onClick = { viewModel.getAll() }) {
                        Text("Get All")
                    }
                    Button(onClick = { viewModel.readResource() }) {
                        Text("Read resource")
                    }
                }
            }
        }
    }
}