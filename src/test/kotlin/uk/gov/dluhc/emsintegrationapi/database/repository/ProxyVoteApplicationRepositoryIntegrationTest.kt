package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

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

    @Test
    fun `should return records by record status order by created date`() {
        val listOfApplications =
            IntStream.rangeClosed(1, 11).mapToObj {
                buildProxyVoteApplication(applicationId = it.toString())
            }.toList()

        proxyVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        val applicationsReceived =
            proxyVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )

        assertThat(applicationsReceived).hasSize(10)
        assertThat(applicationsReceived[0].dateCreated).isBefore(applicationsReceived[9].dateCreated)
        applicationsReceived.forEachIndexed { index, proxyVoteApplication ->
            assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(proxyVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    applicationsReceived[index - 1].dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
            }
        }
    }
}
