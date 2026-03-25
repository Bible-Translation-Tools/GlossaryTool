package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable
import org.bibletranslationtools.glossary.data.Phrase

@Serializable
data class PendingPhrase(
    val phrase: Phrase,
    val user: User,
    val original: Phrase? = null,
    val status: ReviewStatus = ReviewStatus.UNREVIEWED,
    val reviews: List<PhraseReview> = emptyList()
)
