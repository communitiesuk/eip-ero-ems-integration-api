package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovalDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomGssCode
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
    fun `should return records by gss codes and record status order by created date`() {
        // Given

        val listOfApplications =
            IntStream.rangeClosed(1, 30).mapToObj {
                buildProxyVoteApplication(
                    applicationId = it.toString(),
                    buildApprovalDetailsEntity(gssCode = faker.options().option(GSS_CODE, GSS_CODE2))
                )
            }.toList()

        proxyVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        // When
        val applicationsReceived =
            proxyVoteApplicationRepository.findByApprovalDetailsGssCodeInAndStatusOrderByDateCreated(
                listOf(GSS_CODE, getRandomGssCode()),
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )

        // That
        val numberOfApplicationsReceived = applicationsReceived.size
        assertThat(numberOfApplicationsReceived).isEqualTo(10)
        applicationsReceived.forEachIndexed { index, proxyVoteApplication ->
            assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(proxyVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    applicationsReceived[index - 1].dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
                assertThat(proxyVoteApplication.approvalDetails.gssCode).isEqualTo(GSS_CODE)
            }
        }
    }

    @Test
    fun `should not return record by invalid application id and invalid gss codes`() {
        // Given
        val applicationId = "applicationId"
        val proxyApplication =
            buildProxyVoteApplication(
                applicationId = applicationId,
                buildApprovalDetailsEntity(gssCode = GSS_CODE)
            )
        proxyVoteApplicationRepository.saveAndFlush(proxyApplication)
        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                "invalidApplicationId",
                listOf("invalidGGSCode")
            )

        // That
        assertThat(proxyVoteApplication).isNull()
    }

    @Test
    fun `should not return record by application id and invalid gss codes`() {
        // Given
        val applicationId = "applicationId"
        val proxyApplication =
            buildProxyVoteApplication(
                applicationId = applicationId,
                buildApprovalDetailsEntity(gssCode = GSS_CODE)
            )

        proxyVoteApplicationRepository.saveAndFlush(proxyApplication)

        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                applicationId,
                listOf("invalidGGSCode")
            )

        // That
        assertThat(proxyVoteApplication).isNull()
    }
    @Test
    fun `should return record by application id and gss codes`() {
        // Given
        val listOfApplications =
            IntStream.rangeClosed(1, 2).mapToObj {
                buildProxyVoteApplication(
                    applicationId = it.toString(),
                    buildApprovalDetailsEntity(gssCode = GSS_CODE)
                )
            }.toList()

        proxyVoteApplicationRepository.saveAllAndFlush(listOfApplications)
        val applicationId = listOfApplications[0].applicationId

        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                applicationId,
                listOf(GSS_CODE, "9010")
            )

        // That
        assertThat(proxyVoteApplication).isNotNull
        assertThat(proxyVoteApplication?.applicationId).isEqualTo(applicationId)
        assertThat(proxyVoteApplication?.approvalDetails?.gssCode).isEqualTo(GSS_CODE)
    }
}
