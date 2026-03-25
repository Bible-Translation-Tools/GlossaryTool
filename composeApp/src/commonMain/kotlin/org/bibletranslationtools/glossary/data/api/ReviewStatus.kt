package org.bibletranslationtools.glossary.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReviewStatus {
    @SerialName("unreviewed") UNREVIEWED,
    @SerialName("approved") APPROVED,
    @SerialName("rejected") REJECTED,
}