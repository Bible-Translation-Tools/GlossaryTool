package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.large
import glossary.composeapp.generated.resources.medium
import glossary.composeapp.generated.resources.small
import org.jetbrains.compose.resources.stringResource

data class FontSizeSetting(
    val name: String,
    val size: TextUnit,
)

fun String.toFontSizeSetting(): FontSizeSetting {
    return when (this) {
        "small" -> FontSizeSetting(this, 12.8.sp)
        "large" -> FontSizeSetting(this, 20.sp)
        else -> FontSizeSetting(this, 16.sp)
    }
}

@Composable
fun FontSizeSetting.localize(): String {
    return when (this.name) {
        "small" -> stringResource(Res.string.small)
        "large" -> stringResource(Res.string.large)
        else -> stringResource(Res.string.medium)
    }
}

