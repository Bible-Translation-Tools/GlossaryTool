package org.bibletranslationtools.glossary.ui.glossary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.download
import glossary.composeapp.generated.resources.downloading_glossary
import glossary.composeapp.generated.resources.downloading_glossary_hint
import glossary.composeapp.generated.resources.import_glossary_hint
import glossary.composeapp.generated.resources.import_glossary_manually
import glossary.composeapp.generated.resources.import_glossary_title
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.bibletranslationtools.glossary.ui.components.OtpInput
import org.bibletranslationtools.glossary.ui.components.TopAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImportGlossaryScreen(component: ImportGlossaryComponent) {
    val model by component.model.subscribeAsState()

    var code by remember { mutableStateOf("") }
    val focusRequesters = remember { List(5) { FocusRequester() } }

    val focusManager = LocalFocusManager.current
    val keyboardManager = LocalSoftwareKeyboardController.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        component.setTopBar {
            TopAppBar(
                title = "Add Glossary"
            ) {
                component.onBackClicked()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(128.dp))

            if (model.progress == null) {
                Text(
                    text = stringResource(Res.string.import_glossary_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.import_glossary_hint),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OtpInput(
                    code = model.otpCode,
                    enabled = false,
                    focusRequesters = focusRequesters,
                    onAction = { component.onOtpAction(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* Handle download logic */ },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.download),
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            FileKit.openFilePicker()?.let {
                                component.onImportClicked(it)
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.import_glossary_manually),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.downloading_glossary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.downloading_glossary_hint),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                LinearProgressIndicator(
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}