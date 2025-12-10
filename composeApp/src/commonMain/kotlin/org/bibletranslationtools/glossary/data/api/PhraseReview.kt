package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PhraseReview(
    val phraseId: String,
    val status: ReviewStatus,
    val user: User
)
