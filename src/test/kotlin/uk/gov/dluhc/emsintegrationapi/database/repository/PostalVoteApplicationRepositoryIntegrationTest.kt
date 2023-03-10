package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomString
import uk.gov.dluhc.emsintegrationapi.testsupport.validateEntityMaxSizeConstraintViolation

class PostalVoteApplicationRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {
    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Test
    fun `should save a postal vote application`() {
        // Given
        val postalVoteApplication = buildPostalApplication()

        // When
        postalVoteApplicationRepository.saveAndFlush(postalVoteApplication)

        // Then
        val savedApplication =
            postalVoteApplicationRepository.findById(postalVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(postalVoteApplication)
    }

    @Test
    fun `should throw constraint validation error if field value length is more than the max column size`() {
        val postalVoteApplication = buildPostalApplication(
            postalVoteDetails = buildPostalVoteDetailsEntity(
                ballotAddress = buildAddressEntity(street = getRandomString(256), locality = getRandomString(256))
            )
        )
        validateEntityMaxSizeConstraintViolation({
            postalVoteApplicationRepository.saveAndFlush(
                postalVoteApplication
            )
        }, listOf(Pair("street", 255), Pair("locality", 255)))
    }
}
