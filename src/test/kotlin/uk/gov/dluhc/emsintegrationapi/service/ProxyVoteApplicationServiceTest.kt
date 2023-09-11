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
import org.mockito.kotlin.*
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.GSS_CODE2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import java.util.stream.IntStream

@ExtendWith(MockitoExtension::class)
internal class ProxyVoteApplicationServiceTest {

    @Mock
    private lateinit var apiProperties: ApiProperties

    @Mock
    private lateinit var proxyVoteMapper: ProxyVoteMapper

    @Mock
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Mock
    private lateinit var messageSender: MessageSender<EmsConfirmedReceiptMessage>

    @Mock
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    @InjectMocks
    private lateinit var proxyVoteApplicationService: ProxyVoteApplicationService

    companion object {
        private const val DEFAULT_PAGE_SIZE = 100
        private const val CERTIFICATE_SERIAL_NUMBER = "test"
        private val GSS_CODES = listOf(GSS_CODE, GSS_CODE2)
        private val requestSuccess = EMSApplicationResponse()
    }

    @BeforeEach
    private fun setup() {
        given { retrieveGssCodeService.getGssCodeFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER) }.willReturn(
            GSS_CODES
        )
    }

    @Nested
    inner class PageSizeIsNotProvided {
        @BeforeEach
        fun beforeEach() {
            given(apiProperties.defaultPageSize).willReturn(DEFAULT_PAGE_SIZE)
        }

        @AfterEach
        fun afterEach() {
            verify(apiProperties).defaultPageSize
        }

        @Test
        fun `should return maximum of 100 proxy vote applications`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = null)

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = null)

        @Test
        fun `system does not have any records`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = null)
    }

    @Nested
    inner class PageSizeIsProvided {
        @AfterEach
        fun afterEach() = verifyNoInteractions(apiProperties)

        @Test
        fun `should return maximum of 100 proxy vote applications`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = 200)

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = 100)

        @Test
        fun `system does not have any records`() =
            validateFetchProxyVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = 100)
    }

    private fun validateFetchProxyVoteApplications(numberOfRecordsToBeReturned: Int, pageSizeRequested: Int?) {
        // Given
        val savedApplications =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj {
                buildProxyVoteApplication(applicationId = it.toString())
            }.toList()
        val savedApplicationIds = savedApplications.map { it.applicationId }.toList()
        val mockProxyVotes =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj { mock<ProxyVote>() }.toList()

        given(
            proxyVoteApplicationRepository.findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                GSS_CODES,
                RecordStatus.RECEIVED,
                Pageable.ofSize(pageSizeRequested ?: DEFAULT_PAGE_SIZE)
            )
        ).willReturn(savedApplicationIds)
        given { proxyVoteApplicationRepository.findByApplicationIdIn(savedApplicationIds) }.willReturn(savedApplications)
        given { proxyVoteMapper.mapFromEntities(savedApplications) }.willReturn(mockProxyVotes)

        val proxyVoteApplications =
            proxyVoteApplicationService.getProxyVoteApplications(
                certificateSerialNumber = "test",
                pageSize = pageSizeRequested
            )

        assertThat(proxyVoteApplications.pageSize).isEqualTo(numberOfRecordsToBeReturned)
        assertThat(proxyVoteApplications.proxyVotes).isEqualTo(mockProxyVotes)
    }

    @Nested
    inner class ConfirmedReceipt {
        @Test
        fun `should update the record status to be DELETED and send SUCCESS confirmation message`() {
            // Given
            val proxyVoteApplication = buildProxyVoteApplication()
            given(
                proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    proxyVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(proxyVoteApplication)
            // When
            proxyVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER, proxyVoteApplication.applicationId,
                requestSuccess
            )

            // Then
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(proxyVoteApplication.applicationId, EmsConfirmedReceiptMessage.Status.SUCCESS),
                QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should update the record status to be DELETED and send FAILURE confirmation message`() {
            // Given
            val proxyVoteApplication = buildProxyVoteApplication()
            given(
                proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    proxyVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(proxyVoteApplication)
            // When
            val requestFailure = EMSApplicationResponse(status = EMSApplicationStatus.FAILURE, message = ApplicationConstants.EMS_MESSAGE_TEXT, details = ApplicationConstants.EMS_DETAILS_TEXT)
            proxyVoteApplicationService.confirmReceipt(CERTIFICATE_SERIAL_NUMBER, proxyVoteApplication.applicationId, requestFailure)

            // Then
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(
                    id = proxyVoteApplication.applicationId,
                    status = EmsConfirmedReceiptMessage.Status.FAILURE,
                    message = ApplicationConstants.EMS_MESSAGE_TEXT,
                    details = ApplicationConstants.EMS_DETAILS_TEXT
                ),
                QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should throw application not found exception if a given record does not exist in the db`() {
            val applicationId = "SomeId"
            val applicationNotFoundException =
                assertThrows<ApplicationNotFoundException> {
                    proxyVoteApplicationService.confirmReceipt(
                        CERTIFICATE_SERIAL_NUMBER,
                        applicationId,
                        requestSuccess
                    )
                }
            assertThat(applicationNotFoundException.message).isEqualTo("The ${ApplicationType.PROXY.displayName} application could not be found with id `$applicationId`")
            verifyNoInteractions(messageSender)
        }

        @Test
        fun `should ignore the update request and do not send a message if the application status is DELETED`() {
            // Given
            val proxyVoteApplication = buildProxyVoteApplication(
                recordStatus = RecordStatus.DELETED
            )
            given(
                proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
                    proxyVoteApplication.applicationId,
                    GSS_CODES
                )
            ).willReturn(proxyVoteApplication)

            // When
            proxyVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER, proxyVoteApplication.applicationId,
                requestSuccess
            )

            // Then
            verify(proxyVoteApplicationRepository).findByApplicationIdAndApplicationDetailsGssCodeIn(
                proxyVoteApplication.applicationId,
                GSS_CODES
            )
            // Make sure that save and flush did not call
            verifyNoMoreInteractions(proxyVoteApplicationRepository)
            verifyNoMoreInteractions(messageSender)
        }
    }
}
