package org.bibletranslationtools.glossary.domain

enum class DbSettings(val value: String) {
    INIT("init")
}

enum class Settings {
    THEME,
    LOCALE,
    RESOURCE,
    BOOK,
    CHAPTER,
    GLOSSARY
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
