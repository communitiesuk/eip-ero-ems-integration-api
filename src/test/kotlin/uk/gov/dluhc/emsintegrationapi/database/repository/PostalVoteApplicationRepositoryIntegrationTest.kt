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
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

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

    @Test
    fun `should return records by gss codes and record status order by created date`() {
        // Given

        val listOfApplications =
            IntStream.rangeClosed(1, 30).mapToObj {
                buildPostalVoteApplication(
                    applicationId = it.toString(),
                    buildApprovalDetailsEntity(gssCode = faker.options().option(GSS_CODE, GSS_CODE2))
                )
            }.toList()

        postalVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        // When
        val applicationsReceived =
            postalVoteApplicationRepository.findByApprovalDetailsGssCodeInAndStatusOrderByDateCreated(
                listOf(GSS_CODE, "9010"),
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )

        // That
        val numberOfApplicationsReceived = applicationsReceived.size
        assertThat(numberOfApplicationsReceived).isEqualTo(10)
        applicationsReceived.forEachIndexed { index, postalVoteApplication ->
            assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(postalVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    applicationsReceived[index - 1].dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
                assertThat(postalVoteApplication.approvalDetails.gssCode).isEqualTo(GSS_CODE)
            }
        }
    }

    @Test
    fun `should not return record by invalid application id and invalid gss codes`() {
        // Given
        val applicationId = "applicationId"
        val postalApplication =
            buildPostalVoteApplication(
                applicationId = applicationId,
                buildApprovalDetailsEntity(gssCode = GSS_CODE)
            )
        postalVoteApplicationRepository.saveAndFlush(postalApplication)
        // When
        val postalVoteApplication =
            postalVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                "invalidApplicationId",
                listOf("invalidGGSCode")
            )

        // That
        assertThat(postalVoteApplication).isNull()
    }

    @Test
    fun `should not return record by application id and invalid gss codes`() {
        // Given
        val applicationId = "applicationId"
        val postalApplication =
            buildPostalVoteApplication(
                applicationId = applicationId,
                buildApprovalDetailsEntity(gssCode = GSS_CODE)
            )

        postalVoteApplicationRepository.saveAndFlush(postalApplication)

        // When
        val postalVoteApplication =
            postalVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                applicationId,
                listOf("invalidGGSCode")
            )

        // That
        assertThat(postalVoteApplication).isNull()
    }
    @Test
    fun `should return record by application id and gss codes`() {
        // Given
        val listOfApplications =
            IntStream.rangeClosed(1, 2).mapToObj {
                buildPostalVoteApplication(
                    applicationId = it.toString(),
                    buildApprovalDetailsEntity(gssCode = GSS_CODE)
                )
            }.toList()

        postalVoteApplicationRepository.saveAllAndFlush(listOfApplications)
        val applicationId = listOfApplications[0].applicationId

        // When
        val postalVoteApplication =
            postalVoteApplicationRepository.findByApplicationIdAndApprovalDetailsGssCodeIn(
                applicationId,
                listOf(GSS_CODE, "9010")
            )

        // That
        assertThat(postalVoteApplication).isNotNull
        assertThat(postalVoteApplication?.applicationId).isEqualTo(applicationId)
        assertThat(postalVoteApplication?.approvalDetails?.gssCode).isEqualTo(GSS_CODE)
    }
}
