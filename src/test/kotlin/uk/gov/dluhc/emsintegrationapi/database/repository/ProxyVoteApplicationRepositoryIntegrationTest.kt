package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
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
    fun `should save a proxy vote application when isFromApplicationsApi is true`() {
        // Given
        val proxyVoteApplication = buildProxyVoteApplication(isFromApplicationsApi = true)

        // When
        proxyVoteApplicationRepository.saveAndFlush(proxyVoteApplication)

        // Then
        val savedApplication =
            proxyVoteApplicationRepository.findById(proxyVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(proxyVoteApplication)
    }

    @Test
    fun `should save a proxy vote application when isFromApplicationsApi is false`() {
        // Given
        val proxyVoteApplication = buildProxyVoteApplication(isFromApplicationsApi = false)

        // When
        proxyVoteApplicationRepository.saveAndFlush(proxyVoteApplication)

        // Then
        val savedApplication =
            proxyVoteApplicationRepository.findById(proxyVoteApplication.applicationId).get()

        assertThat(savedApplication).usingRecursiveComparison().isEqualTo(proxyVoteApplication)
    }

    @Test
    fun `should return records by gss codes and record status order by created date using two step process`() {
        // Given

        val listOfApplications =
            IntStream.rangeClosed(1, 30).mapToObj {
                buildProxyVoteApplication(
                    applicationId = it.toString(),
                    buildApplicationDetailsEntity(gssCode = faker.options().option(GSS_CODE, GSS_CODE2))
                )
            }.toList()

        proxyVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        // When
        val applicationIdsReceived =
            proxyVoteApplicationRepository.findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                listOf(GSS_CODE, getRandomGssCode()),
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )
        val applicationsReceived = proxyVoteApplicationRepository.findByApplicationIdIn(applicationIdsReceived)

        // That
        val numberOfApplicationsReceived = applicationsReceived.size
        var previousApplication: ProxyVoteApplication? = null
        assertThat(numberOfApplicationsReceived).isEqualTo(10)
        applicationIdsReceived.forEachIndexed { index, proxyVoteApplicationId ->
            val proxyVoteApplication = applicationsReceived.find { it.applicationId == proxyVoteApplicationId }!!
            assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(proxyVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    previousApplication!!.dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
                assertThat(proxyVoteApplication.applicationDetails.gssCode).isEqualTo(GSS_CODE)
            }
            previousApplication = proxyVoteApplication
        }
    }

    @Test
    fun `should not return record by invalid application id and invalid gss codes`() {
        // Given
        val applicationId = "applicationId"
        val proxyApplication =
            buildProxyVoteApplication(
                applicationId = applicationId,
                buildApplicationDetailsEntity(gssCode = GSS_CODE)
            )
        proxyVoteApplicationRepository.saveAndFlush(proxyApplication)
        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
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
                buildApplicationDetailsEntity(gssCode = GSS_CODE)
            )

        proxyVoteApplicationRepository.saveAndFlush(proxyApplication)

        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
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
                    buildApplicationDetailsEntity(gssCode = GSS_CODE)
                )
            }.toList()

        proxyVoteApplicationRepository.saveAllAndFlush(listOfApplications)
        val applicationId = listOfApplications[0].applicationId

        // When
        val proxyVoteApplication =
            proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                applicationId,
                listOf(GSS_CODE, "9010")
            )

        // That
        assertThat(proxyVoteApplication).isNotNull
        assertThat(proxyVoteApplication?.applicationId).isEqualTo(applicationId)
        assertThat(proxyVoteApplication?.applicationDetails?.gssCode).isEqualTo(GSS_CODE)
    }
}
