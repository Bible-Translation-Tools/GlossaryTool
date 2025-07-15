package org.bibletranslationtools.glossary.data

data class Ref(
    val resource: String,
    val book: String,
    val chapter: Int,
    val verse: Int
)