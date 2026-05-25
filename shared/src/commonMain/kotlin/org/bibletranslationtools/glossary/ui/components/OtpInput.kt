package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp

sealed interface OtpAction {
    data class OnEnterChar(val char: String?, val index: Int): OtpAction
    data class OnChangeFieldFocused(val index: Int): OtpAction
    data object OnKeyboardBack: OtpAction
}

@Composable
fun OtpInput(
    code: List<String?>,
    focusRequesters: List<FocusRequester>,
    onAction: (OtpAction) -> Unit,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val totalCount = code.size

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
    ) {
        code.forEachIndexed { index, char ->
            OtpInputField(
                char = char,
                enabled = enabled,
                isLastField = index == totalCount - 1,
                isError = isError,
                focusRequester = focusRequesters[index],
                onFocusChanged = { isFocused ->
                    if (isFocused) {
                        onAction(OtpAction.OnChangeFieldFocused(index))
                    }
                },
                onCharChanged = { newChar ->
                    onAction(OtpAction.OnEnterChar(newChar, index))
                },
                onKeyboardBack = {
                    onAction(OtpAction.OnKeyboardBack)
                },
                onKeyboardAction = if (index == totalCount - 1) onImeAction else null,
                modifier = Modifier.weight(1f)
                    .aspectRatio(1f)
            )
        }
    }
}