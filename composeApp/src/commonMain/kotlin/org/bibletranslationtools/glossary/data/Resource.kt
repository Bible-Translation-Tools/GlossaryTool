package org.bibletranslationtools.glossary.data

data class Resource(
    val slug: String,
    val books: List<Workbook>
)
