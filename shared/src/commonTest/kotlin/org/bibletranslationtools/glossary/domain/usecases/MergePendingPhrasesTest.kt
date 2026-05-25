package org.bibletranslationtools.glossary.domain.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.bibletranslationtools.glossary.data.Phrase
import org.bibletranslationtools.glossary.domain.persistence.GlossaryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergePendingPhrasesTest {

    private val repository: GlossaryRepository = mockk()
    private val mergePendingPhrases = MergePendingPhrases(repository)

    @Test
    fun testMergeSuccess() = runTest {
        val glossaryId = "g1"
        val phrase = Phrase(
            phrase = "Test",
            createdAt = LocalDateTime(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime(2024, 1, 1, 0, 0),
            glossaryId = glossaryId
        )
        val pendingPhrases = listOf(phrase)

        coEvery { repository.getPendingPhrases(glossaryId) } returns pendingPhrases
        coEvery { repository.batchAddPhrases(any()) } returns Unit
        coEvery { repository.deletePendingByGlossary(glossaryId) } returns Unit

        val result = mergePendingPhrases(glossaryId)

        assertTrue(result.success)
        coVerify {
            repository.getPendingPhrases(glossaryId)
            repository.batchAddPhrases(any())
            repository.deletePendingByGlossary(glossaryId)
        }
    }

    @Test
    fun testMergeFailure() = runTest {
        val glossaryId = "g1"
        coEvery { repository.getPendingPhrases(glossaryId) } throws Exception("Database error")

        val result = mergePendingPhrases(glossaryId)

        assertFalse(result.success)
        assertEquals("Database error", result.message)
    }
}
