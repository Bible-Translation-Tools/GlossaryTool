package org.bibletranslationtools.glossary.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.logo
import glossary.composeapp.generated.resources.wa
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

class SplashScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val gradient = Brush.verticalGradient(
            0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            1.0f to MaterialTheme.colorScheme.primary,
            startY = 0.0f,
            endY = 3000.0f
        )

        LaunchedEffect(null) {
            delay(1000)
            navigator.push(HomeScreen())
        }

        Scaffold {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
                    .background(gradient)
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