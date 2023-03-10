package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovedPostalApplication

class ApprovedPostalVoteApplicationRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var approvedPostalVoteApplicationRepository: ApprovedPostalVoteApplicationRepository

    @Test
    fun shouldSaveAnApprovedPostalVoteApplication() {
        // Given
        val approvedPostalVoteApplication = buildApprovedPostalApplication()

        // When
        val savedApplication = approvedPostalVoteApplicationRepository.save(approvedPostalVoteApplication)

        // Then
        assertThat(savedApplication).isNotNull
    }

    @AfterAll
    fun cleanUp() {
        deleteAll(approvedPostalVoteApplicationRepository)
    }
}
