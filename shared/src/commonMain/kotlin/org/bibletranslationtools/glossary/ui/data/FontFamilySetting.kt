package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

class FontFamilySetting(
    override val name: String,
    override val value: FontFamily,
) : FontSetting<FontFamily> {
    @Composable
    override fun localize(): String {
        return "Aa"
    }

    companion object {
        private const val SERIF_NAME = "Serif"
        private const val SANS_SERIF_NAME = "SansSerif"
        private const val MONOSPACE_NAME = "Monospace"

        val SERIF = FontFamilySetting(SERIF_NAME, FontFamily.Serif)
        val SANS_SERIF = FontFamilySetting(SANS_SERIF_NAME, FontFamily.SansSerif)
        val MONOSPACE = FontFamilySetting(MONOSPACE_NAME, FontFamily.Monospace)

        fun of(string: String): FontSetting<FontFamily> {
            return when (string) {
                SERIF_NAME -> SERIF
                MONOSPACE_NAME -> MONOSPACE
                else -> SANS_SERIF
            }
        }
    }
}
