package org.bibletranslationtools.glossary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SettingsSectionDefaults {
    @Composable
    fun titleStyle(
        color: Color = MaterialTheme.colorScheme.outline,
        fontSize: TextUnit = 16.sp,
        fontWeight: FontWeight = FontWeight.W500
    ): TextStyle = TextStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight
    )
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = SettingsSectionDefaults.titleStyle(),
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = titleStyle
        )
        Column {
            content()
        }
    }
}