package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.default
import glossary.composeapp.generated.resources.large
import glossary.composeapp.generated.resources.small
import org.jetbrains.compose.resources.stringResource

data class LineHeightSetting(
    val name: String,
    val height: TextUnit
)

fun String.toLineHeightSetting(): LineHeightSetting {
    return when (this) {
        "small" -> LineHeightSetting(this, 12.8.sp)
        "large" -> LineHeightSetting(this, 20.sp)
        else -> LineHeightSetting(this, 16.sp)
    }
}

@Composable
fun LineHeightSetting.localize(): String {
    return when (this.name) {
        "small" -> stringResource(Res.string.small)
        "large" -> stringResource(Res.string.large)
        else -> stringResource(Res.string.default)
    }
}
