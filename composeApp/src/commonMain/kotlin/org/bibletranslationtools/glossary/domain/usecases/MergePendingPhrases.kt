package org.bibletranslationtools.glossary.domain.usecases

import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository

class MergePendingPhrases(
    private val glossaryRepository: GlossaryRepository
) {
    data class Result(
        val success: Boolean,
        val message: String? = null
    )

    suspend operator fun invoke(glossaryId: String): Result {
        val result = Result(true)
        return try {
            val pendingPhrases = glossaryRepository.getPendingPhrases(glossaryId)
            glossaryRepository.batchAddPhrases(pendingPhrases)
            glossaryRepository.deletePendingByGlossary(glossaryId)
            result
        } catch (e: Exception) {
            result.copy(
                success = false,
                message = e.message
            )
        }
    }
}