package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PhraseReview(
    val phrase: String,
    val status: ReviewStatus,
    val user: User
)
