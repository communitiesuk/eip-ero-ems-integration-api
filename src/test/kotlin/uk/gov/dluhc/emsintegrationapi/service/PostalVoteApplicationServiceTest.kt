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
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.EMS_APPLICATION_PROCESSED_QUEUE
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.EMS_DETAILS_TEXT
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.EMS_MESSAGE_TEXT
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.service.ApplicationType.POSTAL
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
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

    companion object {
        private const val DEFAULT_PAGE_SIZE = 100
        private const val BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE = 150
        private const val FORCE_MAX_PAGE_SIZE = 200
        private const val MORE_THAN_FORCE_MAX_PAGE_SIZE = 250
        private const val CERTIFICATE_SERIAL_NUMBER = "test"
        private val GSS_CODES = listOf(GSS_CODE, GSS_CODE2)
        private val requestSuccess = EMSApplicationResponse()
    }

    @BeforeEach
    public fun setup() {
        given { retrieveGssCodeService.getGssCodesFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER) }.willReturn(
            GSS_CODES
        )
    }

    @Nested
    inner class PageSizeIsNotProvided {

        @BeforeEach
        fun beforeEach() {
            given(apiProperties.defaultPageSize).willReturn(DEFAULT_PAGE_SIZE)
            given(apiProperties.forceMaxPageSize).willReturn(FORCE_MAX_PAGE_SIZE)
        }

        @AfterEach
        fun afterEach() {
            verify(apiProperties).defaultPageSize
            verify(apiProperties, atLeastOnce()).forceMaxPageSize
        }

        @Test
        fun `should return maximum of DEFAULT_PAGE_SIZE postal vote applications`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = DEFAULT_PAGE_SIZE,
                numberOfRecordsToBeRequested = DEFAULT_PAGE_SIZE,
                pageSizeRequested = null
            )

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = 10,
                numberOfRecordsToBeRequested = DEFAULT_PAGE_SIZE,
                pageSizeRequested = null
            )

        @Test
        fun `system does not have any records`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = 0,
                numberOfRecordsToBeRequested = DEFAULT_PAGE_SIZE,
                pageSizeRequested = null
            )
    }

    @Nested
    inner class PageSizeIsProvided {
        @BeforeEach
        fun beforeEach() {
            given(apiProperties.forceMaxPageSize).willReturn(FORCE_MAX_PAGE_SIZE)
        }

        @AfterEach
        fun afterEach() {
            verify(apiProperties, atLeastOnce()).forceMaxPageSize
            verifyNoMoreInteractions(apiProperties)
        }

        @Test
        fun `should request and return maximum of FORCE_MAX_PAGE_SIZE postal vote applications`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = FORCE_MAX_PAGE_SIZE,
                numberOfRecordsToBeRequested = FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = MORE_THAN_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `should request and return the page size requested postal vote applications`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = 10,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `system does not have any records`() =
            validateFetchPostalVoteApplications(
                numberOfRecordsToBeReturned = 0,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )
    }

    private fun validateFetchPostalVoteApplications(
        numberOfRecordsToBeReturned: Int,
        numberOfRecordsToBeRequested: Int,
        pageSizeRequested: Int?
    ) {
        // Given
        val savedApplications =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj {
                buildPostalVoteApplication(applicationId = it.toString())
            }.toList()
        val savedApplicationIds = savedApplications.map { it.applicationId }.toList()
        val mockPostalVotes =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj { mock<PostalVote>() }.toList()

        given(
            postalVoteApplicationRepository.findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                GSS_CODES,
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToBeRequested)
            )
        ).willReturn(savedApplicationIds)
        given { postalVoteApplicationRepository.findByApplicationIdIn(savedApplicationIds) }.willReturn(
            savedApplications
        )
        given { postalVoteMapper.mapFromEntities(savedApplications) }.willReturn(mockPostalVotes)

        val postalVoteApplications =
            postalVoteApplicationService.getPostalVoteApplications(
                certificateSerialNumber = "test",
                pageSize = pageSizeRequested
            )

        assertThat(postalVoteApplications.pageSize).isEqualTo(numberOfRecordsToBeReturned)
        // the attribute name 'proxyVotes' to postalVotes, awaiting final spec from EMS
        assertThat(postalVoteApplications.postalVotes).isEqualTo(mockPostalVotes)
    }

    @Nested
    inner class ConfirmedReceipt {
        @Test
        fun `should update the record status to be DELETED and send SUCCESS confirmation message`() {
            // Given
            val postalVoteApplication = buildPostalVoteApplication()
            given(
                postalVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    postalVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(postalVoteApplication)
            // When
            postalVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER,
                postalVoteApplication.applicationId,
                requestSuccess
            )

            // Then
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(
                    postalVoteApplication.applicationId,
                    EmsConfirmedReceiptMessage.Status.SUCCESS
                ),
                DELETED_POSTAL_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should send SUCCESS confirmation message to EMS_APPLICATION_PROCESSED_QUEUE`() {
            // Given
            val postalVoteApplication = buildPostalVoteApplication(isFromApplicationsApi = true)
            given(
                postalVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    postalVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(postalVoteApplication)
            // When
            postalVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER,
                postalVoteApplication.applicationId,
                requestSuccess
            )

            // Then
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(
                    postalVoteApplication.applicationId,
                    EmsConfirmedReceiptMessage.Status.SUCCESS
                ),
                EMS_APPLICATION_PROCESSED_QUEUE
            )
        }

        @Test
        fun `should update the record status to be DELETED and send FAILURE confirmation message`() {
            // Given
            val postalVoteApplication = buildPostalVoteApplication()
            given(
                postalVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    postalVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(postalVoteApplication)
            // When
            val requestFailure = EMSApplicationResponse(
                status = EMSApplicationStatus.FAILURE,
                message = EMS_MESSAGE_TEXT,
                details = EMS_DETAILS_TEXT
            )
            postalVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER,
                postalVoteApplication.applicationId,
                requestFailure
            )

            // Then
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(
                    id = postalVoteApplication.applicationId,
                    status = EmsConfirmedReceiptMessage.Status.FAILURE,
                    message = EMS_MESSAGE_TEXT,
                    details = EMS_DETAILS_TEXT
                ),
                DELETED_POSTAL_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should throw application not found exception if a given record does not exist in the db`() {
            val applicationId = "SomeId"
            val applicationNotFoundException =
                assertThrows<ApplicationNotFoundException> {
                    postalVoteApplicationService.confirmReceipt(
                        CERTIFICATE_SERIAL_NUMBER,
                        applicationId,
                        requestSuccess
                    )
                }
            assertThat(applicationNotFoundException.message).isEqualTo("The ${POSTAL.displayName} application could not be found with id `$applicationId`")
            verifyNoInteractions(messageSender)
        }

        @Test
        fun `should ignore the update request and do not send a message if the application status is DELETED`() {
            // Given
            val postalVoteApplication = buildPostalVoteApplication(
                recordStatus = RecordStatus.DELETED
            )
            given(
                postalVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    postalVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(postalVoteApplication)

            // When
            postalVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER,
                postalVoteApplication.applicationId,
                requestSuccess
            )

            // Then
            verify(postalVoteApplicationRepository).findByApplicationIdAndApplicationDetailsGssCodeIn(
                postalVoteApplication.applicationId,
                GSS_CODES
            )
            // Make sure that save and flush did not call
            verifyNoMoreInteractions(postalVoteApplicationRepository)
            verifyNoMoreInteractions(messageSender)
        }
    }
}
