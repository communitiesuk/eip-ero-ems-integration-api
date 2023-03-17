package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication

class ProxyVoteApplicationRepositoryIntegrationTest : AbstractRepositoryIntegrationTest() {
    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Test
    fun `should save a proxy vote application`() {
        // Given
        val proxyVoteApplication = buildProxyVoteApplication()

        // When
        proxyVoteApplicationRepository.saveAndFlush(proxyVoteApplication)

        // Then
        val savedApplication =
            proxyVoteApplicationRepository.findById(proxyVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(proxyVoteApplication)
    }
}
