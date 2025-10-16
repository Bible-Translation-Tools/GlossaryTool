package org.bibletranslationtools.glossary.data

import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.RefEntity
import org.bibletranslationtools.glossary.Utils.generateUUID

@Serializable
data class Ref(
    val book: String,
    val chapter: String,
    val verse: String,
    val phraseId: String? = null,
    val id: String? = null
) {
    fun getVerseText(resource: Resource): String {
        return resource.books
            .single { it.slug == book }
            .chapters.single { it.number.toString() == chapter }
            .verses.single { it.number == verse }
            .text
    }

    fun getChapterVerses(resource: Resource): List<Verse> {
        return resource.books
            .single { it.slug == book }
            .chapters.single { it.number.toString() == chapter }
            .verses
    }

    override fun toString(): String {
        return "${book.uppercase()} $chapter:$verse"
    }
}

fun RefEntity.toModel(): Ref {
    return Ref(
        book = book,
        chapter = chapter,
        verse = verse,
        phraseId = phraseId,
        id = id
    )
}

fun Ref.toEntity(): RefEntity {
    return RefEntity(
        book = book,
        chapter = chapter,
        verse = verse,
        phraseId = phraseId!!,
        id = id ?: generateUUID(),
    )
}