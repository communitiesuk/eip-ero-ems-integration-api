package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovedPostalApplication

class ApprovedPostalVoteApplicationRepositoryTest : AbstractRepositoryTest() {
    @Autowired
    private lateinit var approvedPostalVoteApplicationRepository: ApprovedPostalVoteApplicationRepository
    @Test
    fun `should save an approved postal vote application`() {
        // Given
        val approvedPostalVoteApplication = buildApprovedPostalApplication()

        // When
        approvedPostalVoteApplicationRepository.saveAndFlush(approvedPostalVoteApplication)

        // Then
        val savedApplication =
            approvedPostalVoteApplicationRepository.findById(approvedPostalVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(approvedPostalVoteApplication)
    }
}
