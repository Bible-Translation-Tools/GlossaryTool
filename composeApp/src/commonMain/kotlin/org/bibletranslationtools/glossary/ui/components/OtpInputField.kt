package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpInputField(
    char: String?,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onCharChanged: (String) -> Unit,
    onKeyboardBack: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
//    var text by remember {
//        mutableStateOf(
//            TextFieldValue(
//                text = char ?: "",
//                selection = TextRange(
//                    index = if (char != null) 1 else 0
//                )
//            )
//        )
//    }

    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    val borderSize = if (isFocused) 2.dp else 1.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = borderSize,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        BasicTextField(
            value = char?.uppercase() ?: "",
            onValueChange = { newText ->
                if (newText.length <= 1) {
                    onCharChanged(newText.uppercase())
                }
            },
            enabled = enabled,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii
            ),
            modifier = Modifier
                .padding(10.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusChanged(it.isFocused)
                }
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp) {
                        when (event.key) {
                            Key.Delete, Key.Backspace -> {
                                onKeyboardBack()
                            }
                        }
                    }
                    false
                }
        )
    }
}