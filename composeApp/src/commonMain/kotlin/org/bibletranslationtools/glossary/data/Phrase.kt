package org.bibletranslationtools.glossary.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.PendingPhraseEntity
import org.bibletranslationtools.glossary.PhraseEntity
import org.bibletranslationtools.glossary.Utils.generateUUID
import org.bibletranslationtools.glossary.Utils.getCurrentTime
import org.bibletranslationtools.glossary.data.api.ReviewStatus
import org.bibletranslationtools.glossary.toLocalDateTime
import org.bibletranslationtools.glossary.toTimestamp

enum class PhraseWorkflow {
    DRAFT,
    SAVED,
    PENDING,
    IN_REVIEW,
    REVIEWED
}

@Serializable
data class Phrase(
    val phrase: String,
    val spelling: String = "",
    val description: String = "",
    val audio: String? = null,
    val pending: Boolean = false,
    val status: ReviewStatus? = null,
    @Contextual val createdAt: LocalDateTime = getCurrentTime(),
    @Contextual val updatedAt: LocalDateTime = getCurrentTime(),
    val glossaryId: String? = null,
    val id: String? = null
) {
    val workflow: PhraseWorkflow
        get() = when {
            id == null -> PhraseWorkflow.DRAFT
            !pending -> PhraseWorkflow.SAVED
            status == null -> PhraseWorkflow.PENDING
            status == ReviewStatus.UNREVIEWED -> PhraseWorkflow.IN_REVIEW
            else -> PhraseWorkflow.REVIEWED
        }
}

fun PhraseEntity.toModel(): Phrase {
    return Phrase(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio,
        pending = false,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        glossaryId = glossaryId,
        id = id
    )
}

fun PendingPhraseEntity.toModel(): Phrase {
    return Phrase(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio,
        pending = true,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        glossaryId = glossaryId,
        id = id
    )
}

fun Phrase.toEntity(): PhraseEntity {
    return PhraseEntity(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio ?: "",
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        glossaryId = glossaryId!!,
        id = id ?: generateUUID()
    )
}

fun Phrase.toPendingEntity(): PendingPhraseEntity {
    return PendingPhraseEntity(
        phrase = phrase,
        spelling = spelling,
        description = description,
        audio = audio ?: "",
        createdAt = createdAt.toTimestamp(),
        updatedAt = updatedAt.toTimestamp(),
        glossaryId = glossaryId!!,
        id = id ?: generateUUID()
    )
}
