package org.bibletranslationtools.glossary.data

data class Workbook(
    val sort: Int,
    val slug: String,
    val title: String,
    val language: Language,
    private val readChapters: () -> List<Chapter>
) {
    val chapters: List<Chapter> by lazy(readChapters)
}
