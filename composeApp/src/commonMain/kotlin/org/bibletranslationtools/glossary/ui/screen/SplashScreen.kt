package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.logo
import glossary.composeapp.generated.resources.wa
import org.bibletranslationtools.glossary.domain.Settings
import org.bibletranslationtools.glossary.ui.screenmodel.SplashScreenModel
import org.jetbrains.compose.resources.painterResource

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SplashScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        val state by screenModel.state.collectAsStateWithLifecycle()
        val selectedResource by rememberStringSetting(
            Settings.RESOURCE.name,
            "en_ulb"
        )
        var selectedGlossary by rememberStringSettingOrNull(
            Settings.GLOSSARY.name
        )

        val gradient = Brush.verticalGradient(
            0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            1.0f to MaterialTheme.colorScheme.primary,
            startY = 0.0f,
            endY = 3000.0f
        )

        LaunchedEffect(state.initDone) {
            if (state.initDone) {
                navigator.push(TabbedScreen())
            } else {
                screenModel.initializeApp(
                    selectedResource,
                    selectedGlossary
                )
            }
        }

        Scaffold { paddingValues ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
                    .background(gradient)
                    .padding(paddingValues)
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "app_logo",
                    modifier = Modifier.align(Alignment.Center)
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    state.message?.let { message ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimary
                            )

                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(128.dp))
                    }

                    Image(
                        painter = painterResource(Res.drawable.wa),
                        contentDescription = "wa_logo"
                    )
                }
            }
        }
    }
}