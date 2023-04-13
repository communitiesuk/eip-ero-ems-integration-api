package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
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
    fun `should return records by record status order by created date`() {
        val listOfApplications =
            IntStream.rangeClosed(1, 11).mapToObj {
                buildPostalVoteApplication(applicationId = it.toString())
            }.toList()

        postalVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        val applicationsReceived =
            postalVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )

        assertThat(applicationsReceived).hasSize(10)
        assertThat(applicationsReceived[0].dateCreated).isBefore(applicationsReceived[9].dateCreated)
        applicationsReceived.forEachIndexed { index, postalVoteApplication ->
            assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(postalVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    applicationsReceived[index - 1].dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
            }
        }
    }

    @Test
    fun `should return records by gss codes and record status order by created date`() {
        // Given
        val approvalDetailsValid = buildApprovalDetailsEntity(gssCode = GSS_CODE_1)
        val approvalDetailsInvalid = buildApprovalDetailsEntity(gssCode = "5678")
        val listOfApplications =
            IntStream.rangeClosed(1, 30).mapToObj {
                val approvalDetails = if (it % 2 == 0) {
                    approvalDetailsValid
                } else {
                    approvalDetailsInvalid
                }
                buildPostalVoteApplication(applicationId = it.toString(), approvalDetails = approvalDetails)
            }.toList()

        postalVoteApplicationRepository.saveAllAndFlush(listOfApplications)

        // When
        val applicationsReceived =
            postalVoteApplicationRepository.findByApprovalDetailsGssCodeInAndStatusOrderByDateCreated(
                listOf(GSS_CODE_1, "9010"),
                RecordStatus.RECEIVED,
                Pageable.ofSize(10)
            )

        // That
        val numberOfApplicationsReceived = applicationsReceived.size
        assertThat(numberOfApplicationsReceived).isLessThan(11)
        assertThat(applicationsReceived[0].dateCreated).isBefore(applicationsReceived[numberOfApplicationsReceived - 1].dateCreated)
        applicationsReceived.forEachIndexed { index, postalVoteApplication ->
            assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.RECEIVED)
            if (index > 0) {
                assertThat(postalVoteApplication.dateCreated!!.truncatedTo(ChronoUnit.SECONDS)).isAfterOrEqualTo(
                    applicationsReceived[index - 1].dateCreated!!.truncatedTo(
                        ChronoUnit.SECONDS
                    )
                )
                assertThat(postalVoteApplication.approvalDetails.gssCode).isEqualTo(GSS_CODE_1)
            }
        }
    }
}
