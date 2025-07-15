package org.bibletranslationtools.glossary.data

import org.bibletranslationtools.glossary.RefEntity

data class Ref(
    val resource: String,
    val book: String,
    val chapter: String,
    val verse: String
)

fun RefEntity.toRef(): Ref {
    return Ref(
        resource = this.resource,
        book = this.book,
        chapter = this.chapter,
        verse = this.verse
    )
}