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
        private const val BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE = 150
        private const val FORCE_MAX_PAGE_SIZE = 200
        private const val MORE_THAN_FORCE_MAX_PAGE_SIZE = 250
        private const val CERTIFICATE_SERIAL_NUMBER = "test"
        private val GSS_CODES = listOf(GSS_CODE, GSS_CODE2)
        private val requestSuccess = EMSApplicationResponse()
    }

    @BeforeEach
    public fun setup() {
        given { retrieveGssCodeService.getGssCodeFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER) }.willReturn(
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
        fun `should return maximum of DEFAULT_PAGE_SIZE proxy vote applications`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = DEFAULT_PAGE_SIZE,
                numberOfRecordsToBeRequested = DEFAULT_PAGE_SIZE,
                pageSizeRequested = null
            )

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = 10,
                numberOfRecordsToBeRequested = DEFAULT_PAGE_SIZE,
                pageSizeRequested = null
            )

        @Test
        fun `system does not have any records`() =
            validateFetchProxyVoteApplications(
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
        fun `should request and return maximum of FORCE_MAX_PAGE_SIZE proxy vote applications`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = FORCE_MAX_PAGE_SIZE,
                numberOfRecordsToBeRequested = FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = MORE_THAN_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `should request and return the page size requested proxy vote applications`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `system does not have requested number of records in the DB`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = 10,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )

        @Test
        fun `system does not have any records`() =
            validateFetchProxyVoteApplications(
                numberOfRecordsToBeReturned = 0,
                numberOfRecordsToBeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE,
                pageSizeRequested = BETWEEN_DEFAULT_AND_FORCE_MAX_PAGE_SIZE
            )
    }

    private fun validateFetchProxyVoteApplications(
        numberOfRecordsToBeReturned: Int,
        numberOfRecordsToBeRequested: Int,
        pageSizeRequested: Int?
    ) {
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
                Pageable.ofSize(numberOfRecordsToBeRequested)
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
                EmsConfirmedReceiptMessage(
                    proxyVoteApplication.applicationId,
                    EmsConfirmedReceiptMessage.Status.SUCCESS
                ),
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
            val requestFailure = EMSApplicationResponse(
                status = EMSApplicationStatus.FAILURE,
                message = ApplicationConstants.EMS_MESSAGE_TEXT,
                details = ApplicationConstants.EMS_DETAILS_TEXT
            )
            proxyVoteApplicationService.confirmReceipt(
                CERTIFICATE_SERIAL_NUMBER,
                proxyVoteApplication.applicationId,
                requestFailure
            )

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
