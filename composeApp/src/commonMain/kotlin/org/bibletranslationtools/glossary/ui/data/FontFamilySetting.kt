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
        val SERIF = FontFamilySetting("Serif", FontFamily.Serif)
        val SANS_SERIF = FontFamilySetting("SansSerif", FontFamily.SansSerif)
        val CURSIVE = FontFamilySetting("Cursive", FontFamily.Cursive)

        fun of(string: String): FontSetting<FontFamily> {
            return when (string) {
                "Serif" -> SERIF
                "Cursive" -> CURSIVE
                else -> SANS_SERIF
            }
        }
    }
}
