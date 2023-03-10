package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovedPostalApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomString
import uk.gov.dluhc.emsintegrationapi.testsupport.validateConstraintViolation

class ApprovedPostalVoteApplicationRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {
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

    @Test
    fun `should throw constraint validation error if field value length is more the max column size`() {
        val approvedPostalVoteApplication = buildApprovedPostalApplication(
            postalVoteDetails = buildPostalVoteDetailsEntity(
                ballotAddress = buildAddressEntity(street = getRandomString(256), locality = getRandomString(256))
            )
        )
        validateConstraintViolation({
            approvedPostalVoteApplicationRepository.saveAndFlush(
                approvedPostalVoteApplication
            )
        }, listOf(Pair("street", 255), Pair("locality", 255)))
    }
}
