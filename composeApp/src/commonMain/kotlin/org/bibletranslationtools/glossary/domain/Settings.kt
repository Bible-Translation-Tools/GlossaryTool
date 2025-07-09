package org.bibletranslationtools.glossary.domain

enum class Settings {
    THEME,
    LOCALE
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

enum class Locales(val value: String) {
    EN("English"),
    RU("Русский")
}
