package org.bibletranslationtools.glossary.ui.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

data class FontFamilySetting(
    val name: String,
    val family: FontFamily,
)

fun String.toFontFamilySetting(): FontFamilySetting {
    return when (this) {
        "Serif" -> FontFamilySetting(this, FontFamily.Serif)
        "Cursive" -> FontFamilySetting(this, FontFamily.Cursive)
        else -> FontFamilySetting(this, FontFamily.SansSerif)
    }
}

@Composable
fun FontFamilySetting.localize(): String {
    return "Aa"
}
