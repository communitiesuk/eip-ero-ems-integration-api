package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication

class PostalVoteApplicationRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {
    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Test
    fun `should save a postal vote application`() {
        // Given
        val postalVoteApplication = buildPostalVoteApplication()

        // When
        postalVoteApplicationRepository.saveAndFlush(postalVoteApplication)

        // Then
        val savedApplication =
            postalVoteApplicationRepository.findById(postalVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(postalVoteApplication)
    }
}
