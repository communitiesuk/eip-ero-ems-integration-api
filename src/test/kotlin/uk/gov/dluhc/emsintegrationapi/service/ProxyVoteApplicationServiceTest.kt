package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.data.domain.Pageable
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
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
}
