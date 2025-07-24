package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberStringSetting
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.logo
import glossary.composeapp.generated.resources.wa
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.navigation.LocalAppState
import org.bibletranslationtools.glossary.ui.screenmodel.SplashEvent
import org.bibletranslationtools.glossary.ui.screenmodel.SplashScreenModel
import org.jetbrains.compose.resources.painterResource

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SplashScreenModel>()
        val navigator = LocalNavigator.currentOrThrow
        val appState = LocalAppState.currentOrThrow

        val state by viewModel.state.collectAsStateWithLifecycle()

        val selectedResource by rememberStringSetting(
            Settings.RESOURCE.name,
            "en_ulb"
        )

        val gradient = Brush.verticalGradient(
            0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            1.0f to MaterialTheme.colorScheme.primary,
            startY = 0.0f,
            endY = 3000.0f
        )

        LaunchedEffect(state.initDone) {
            if (state.initDone) {
                appState.resource = state.resource
                navigator.push(TabbedScreen())
            } else {
                viewModel.onEvent(SplashEvent.InitApp(selectedResource))
            }
        }

        Scaffold { paddingValues ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
                    .background(gradient)
                    .padding(paddingValues)
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(0.9F)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = "app_logo"
                        )
                    }
                    Image(
                        modifier = Modifier.weight(0.1F),
                        painter = painterResource(Res.drawable.wa),
                        contentDescription = "wa_logo"
                    )
                }
            }
        }
    }
}