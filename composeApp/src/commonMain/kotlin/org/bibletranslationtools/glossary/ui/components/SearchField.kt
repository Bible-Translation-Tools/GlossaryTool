package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun SearchField(
    searchQuery: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    colors: CustomTextFieldColors = CustomTextFieldDefaults.colors(),
    onFocusChange: (Boolean) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var searchFocused by remember { mutableStateOf(false) }
    val width by remember {
        derivedStateOf { if (searchFocused) 200.dp else 60.dp }
    }

    CustomOutlinedTextField(
        value = searchQuery,
        onValueChange = onValueChange,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        placeholder = placeholder,
        trailingIcon = {
            if (searchFocused) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        onValueChange(TextFieldValue(""))
                        focusManager.clearFocus()
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        modifier = modifier.width(width)
            .height(48.dp)
            .fillMaxWidth()
            .onFocusChanged {
                searchFocused = it.isFocused
                onFocusChange(it.isFocused)
            }
    )
}