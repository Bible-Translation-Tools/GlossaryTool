package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.bibletranslationtools.glossary.ui.viewmodel.HomeEvent
import org.bibletranslationtools.glossary.ui.viewmodel.HomeViewModel
import org.bibletranslationtools.glossary.ui.viewmodel.SplashEvent

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeViewModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()
        val event by viewModel.event.collectAsStateWithLifecycle(HomeEvent.Idle)

        val navigator = LocalNavigator.currentOrThrow
        val coroutine = rememberCoroutineScope()

        val bookScrollState = rememberScrollState()

        Scaffold { paddingValues ->
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column {
                    Text("Home")
                    Button(onClick = { viewModel.insert() }) {
                        Text("Insert")
                    }
                    Button(onClick = { viewModel.getAll() }) {
                        Text("Get All")
                    }
                    Button(onClick = { viewModel.onEvent(HomeEvent.LoadBooks) }) {
                        Text("Load Books")
                    }
                    Button(onClick = {
                        state.books.singleOrNull { it.slug == "jas" }?.let {
                            viewModel.onEvent(HomeEvent.LoadBook(it))
                        }
                    }) {
                        Text("Load James")
                    }
                    state.activeBook?.let { book ->
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("\n")
                        book.chapters.forEach { chapter ->
                            stringBuilder.append("Chapter ${chapter.name}\n\n")
                            chapter.verses.forEach { verse ->
                                stringBuilder.append("${verse.name}. ${verse.text}\n")
                            }
                            stringBuilder.append("\n")
                        }
                        Text(
                            text = stringBuilder.toString(),
                            modifier = Modifier.verticalScroll(bookScrollState)
                        )
                    }
                }
            }
        }
    }
}