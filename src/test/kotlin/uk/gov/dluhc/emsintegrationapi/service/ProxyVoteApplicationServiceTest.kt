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
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import java.util.Optional
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

    @InjectMocks
    private lateinit var proxyVoteApplicationService: ProxyVoteApplicationService

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

        val savedApplications =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj {
                buildProxyVoteApplication(applicationId = it.toString())
            }.toList()
        val mockProxyVotes =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj { mock<ProxyVote>() }.toList()

        given(
            proxyVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(pageSizeRequested ?: defaultPageSize)
            )
        ).willReturn(savedApplications)
        given { proxyVoteMapper.mapFromEntities(savedApplications) }.willReturn(mockProxyVotes)

        val proxyVoteAcceptedResponse =
            proxyVoteApplicationService.getProxyVoteApplications(
                certificateSerialNumber = "test",
                pageSize = pageSizeRequested
            )

        assertThat(proxyVoteAcceptedResponse.pageSize).isEqualTo(numberOfRecordsToBeReturned)
        assertThat(proxyVoteAcceptedResponse.proxyVotes).isEqualTo(mockProxyVotes)
    }

    @Nested
    inner class ConfirmReceipt {
        @Test
        fun `should update the record status to be DELETED and send a confirmation message`() {
            // Given
            val proxyVoteApplicationCaptor = argumentCaptor<ProxyVoteApplication>()
            val proxyVoteApplication = buildProxyVoteApplication()
            given(proxyVoteApplicationRepository.findById(proxyVoteApplication.applicationId)).willReturn(
                Optional.of(
                    proxyVoteApplication
                )
            )
            // When
            proxyVoteApplicationService.confirmReceipt(proxyVoteApplication.applicationId)

            // Then
            verify(proxyVoteApplicationRepository).saveAndFlush(proxyVoteApplicationCaptor.capture())
            val applicationSaved = proxyVoteApplicationCaptor.firstValue
            assertThat(applicationSaved.status).isEqualTo(RecordStatus.DELETED)
            assertThat(applicationSaved.updatedBy).isEqualTo(SourceSystem.EMS)
            verify(messageSender).send(
                EmsConfirmedReceiptMessage(proxyVoteApplication.applicationId),
                QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
            )
        }

        @Test
        fun `should throw application not found exception if a given record does not exist in the db`() {
            val applicationId = "SomeId"
            val applicationNotFoundException =
                assertThrows<ApplicationNotFoundException> { proxyVoteApplicationService.confirmReceipt(applicationId) }
            assertThat(applicationNotFoundException.message).isEqualTo("The ${ApplicationType.PROXY.displayName} application could not be found with id `$applicationId`")
            verifyNoInteractions(messageSender)
        }

        @Test
        fun `should ignore the update request and do not send a message if the application status is DELETED`() {
            // Given
            val proxyVoteApplication = buildProxyVoteApplication(
                recordStatus = RecordStatus.DELETED
            )
            given(proxyVoteApplicationRepository.findById(proxyVoteApplication.applicationId)).willReturn(
                Optional.of(
                    proxyVoteApplication
                )
            )
            // When
            proxyVoteApplicationService.confirmReceipt(proxyVoteApplication.applicationId)

            // Then
            verify(proxyVoteApplicationRepository).findById(proxyVoteApplication.applicationId)
            // Make sure that save and flush did not call
            verifyNoMoreInteractions(proxyVoteApplicationRepository)
            verifyNoMoreInteractions(messageSender)
        }
    }
}
