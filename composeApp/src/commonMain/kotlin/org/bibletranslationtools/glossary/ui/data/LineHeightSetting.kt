package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import glossary.composeapp.generated.resources.Res
import glossary.composeapp.generated.resources.default
import glossary.composeapp.generated.resources.large
import glossary.composeapp.generated.resources.small
import org.jetbrains.compose.resources.stringResource

class LineHeightSetting(
    override val name: String,
    override val value: TextUnit
) : FontSetting<TextUnit> {
    @Composable
    override fun localize(): String {
        return when (this.name) {
            "small" -> stringResource(Res.string.small)
            "large" -> stringResource(Res.string.large)
            else -> stringResource(Res.string.default)
        }
    }

    companion object {
        val SMALL = LineHeightSetting("small", 12.8.sp)
        val DEFAULT = LineHeightSetting("default", 16.sp)
        val LARGE = LineHeightSetting("large", 20.sp)

        fun of(string: String): FontSetting<TextUnit> {
            return when (string) {
                "small" -> SMALL
                "large" -> LARGE
                else -> DEFAULT
            }
        }
    }
}
