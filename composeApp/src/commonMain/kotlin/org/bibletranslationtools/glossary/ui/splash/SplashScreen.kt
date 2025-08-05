package org.bibletranslationtools.glossary.ui.splash

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.burnoo.compose.remembersetting.rememberStringSettingOrNull
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.logo
import glossary.composeapp.generated.resources.wa
import org.bibletranslationtools.glossary.domain.Settings
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(component: SplashComponent) {
    val model by component.model.subscribeAsState()

    val selectedResource by rememberStringSetting(
        Settings.RESOURCE,
        "en_ulb"
    )
    val selectedGlossary by rememberStringSettingOrNull(
        Settings.GLOSSARY
    )

    val gradient = Brush.verticalGradient(
        0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        1.0f to MaterialTheme.colorScheme.primary,
        startY = 0.0f,
        endY = 3000.0f
    )

    LaunchedEffect(Unit) {
        component.initializeApp(
            resource = selectedResource,
            glossaryCode = selectedGlossary
        )
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
                model.message?.let { message ->
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