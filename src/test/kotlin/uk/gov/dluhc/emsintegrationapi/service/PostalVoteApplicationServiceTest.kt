package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.service.ApplicationType.POSTAL
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import java.util.Optional
import java.util.stream.IntStream

@ExtendWith(MockitoExtension::class)
internal class PostalVoteApplicationServiceTest {

    @Mock
    private lateinit var apiProperties: ApiProperties

    @Mock
    private lateinit var postalVoteMapper: PostalVoteMapper

    @Mock
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Mock
    private lateinit var messageSender: MessageSender<EmsConfirmedReceiptMessage>

    @Mock
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    @InjectMocks
    private lateinit var postalVoteApplicationService: PostalVoteApplicationService

    private val defaultPageSize = 100

    @Nested
    inner class PageSizeIsNotProvided {

        @BeforeEach
        fun beforeEach() {
            given(apiProperties.defaultPageSize).willReturn(defaultPageSize)
        }

        @AfterEach
        fun afterEach() {
            verify(apiProperties).defaultPageSize
        }

        @Test
        fun `should return maximum of 100 postal vote applications`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = null)

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = null)

        @Test
        fun `system does not have any records`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = null)
    }

    @Nested
    inner class PageSizeIsProvided {
        @AfterEach
        fun afterEach() {
            verifyNoInteractions(apiProperties)
        }

        @Test
        fun `should return maximum of 100 postal vote applications`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = 200)

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = 100)

        @Test
        fun `system does not have any records`() =
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = 100)
    }

    private fun validateFetchPostalVoteApplications(numberOfRecordsToBeReturned: Int, pageSizeRequested: Int?) {
        val certificateSerialNumber = "test"
        val gssCodes = listOf(GSS_CODE1, GSS_CODE2)
        given { retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber) }.willReturn(
            listOf(
                GSS_CODE1, GSS_CODE2
            )
        )
        // Given
        val savedApplications =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj {
                buildPostalVoteApplication(applicationId = it.toString())
            }.toList()
        val mockPostalVotes =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj { mock<PostalVote>() }.toList()

        given(
            postalVoteApplicationRepository.findByApprovalDetailsGssCodeInAndStatusOrderByDateCreated(
                gssCodes,
                RecordStatus.RECEIVED,
                Pageable.ofSize(pageSizeRequested ?: defaultPageSize)
            )
        ).willReturn(savedApplications)
        given { postalVoteMapper.mapFromEntities(savedApplications) }.willReturn(mockPostalVotes)

        val postalVoteAcceptedResponse =
            postalVoteApplicationService.getPostalVoteApplications(
                certificateSerialNumber = "test",
                pageSize = pageSizeRequested
            )

        assertThat(postalVoteAcceptedResponse.pageSize).isEqualTo(numberOfRecordsToBeReturned)
        // the attribute name 'proxyVotes' to postalVotes, awaiting final spec from EMS
        assertThat(postalVoteAcceptedResponse.proxyVotes).isEqualTo(mockPostalVotes)
    }

    @Nested
    inner class ConfirmReceipt {
        @Test
        fun `should update the record status to be DELETED and send a confirmation message`() {
            // Given
            val postalVoteApplicationCaptor = argumentCaptor<PostalVoteApplication>()
            val postalVoteApplication = buildPostalVoteApplication()
            given(postalVoteApplicationRepository.findById(postalVoteApplication.applicationId)).willReturn(
                Optional.of(
                    postalVoteApplication
                )
            )
            // When
            postalVoteApplicationService.confirmReceipt(postalVoteApplication.applicationId)

            // Then
            verify(postalVoteApplicationRepository).saveAndFlush(postalVoteApplicationCaptor.capture())
            val applicationSaved = postalVoteApplicationCaptor.firstValue
            assertThat(applicationSaved.status).isEqualTo(RecordStatus.DELETED)
            assertThat(applicationSaved.updatedBy).isEqualTo(SourceSystem.EMS)
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(postalVoteApplication.applicationId),
                DELETED_POSTAL_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should throw application not found exception if a given record does not exist in the db`() {
            val applicationId = "SomeId"
            val applicationNotFoundException =
                assertThrows<ApplicationNotFoundException> { postalVoteApplicationService.confirmReceipt(applicationId) }
            assertThat(applicationNotFoundException.message).isEqualTo("The ${POSTAL.displayName} application could not be found with id `$applicationId`")
            verifyNoInteractions(messageSender)
        }

        @Test
        fun `should ignore the update request and do not send a message if the application status is DELETED`() {
            // Given
            val postalVoteApplication = buildPostalVoteApplication(
                recordStatus = RecordStatus.DELETED
            )
            given(postalVoteApplicationRepository.findById(postalVoteApplication.applicationId)).willReturn(
                Optional.of(
                    postalVoteApplication
                )
            )
            // When
            postalVoteApplicationService.confirmReceipt(postalVoteApplication.applicationId)

            // Then
            verify(postalVoteApplicationRepository).findById(postalVoteApplication.applicationId)
            // Make sure that save and flush did not call
            verifyNoMoreInteractions(postalVoteApplicationRepository)
            verifyNoMoreInteractions(messageSender)
        }
    }
}
