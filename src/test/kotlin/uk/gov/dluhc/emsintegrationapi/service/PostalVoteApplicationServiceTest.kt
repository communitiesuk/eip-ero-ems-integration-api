package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
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

    @InjectMocks
    private lateinit var postalVoteApplicationService: PostalVoteApplicationService

    private val defaultPageSize = 100

    @Nested
    inner class PageSizeIsNotProvided {
        @AfterEach
        fun afterEach() {
            verify(apiProperties).defaultPageSize
        }
        @Test
        fun `should return maximum of 100 postal vote applications`() {
            // Given
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = null)
        }

        @Test
        fun `system does not have requested number of records in the DB`() {
            // Given validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = null)
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = null)
        }

        @Test
        fun `system does not have any records`() {
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = null)
        }
    }

    @Nested
    inner class PageSizeIsProvided {
        @AfterEach
        fun afterEach() {
            verifyNoInteractions(apiProperties)
        }

        @Test
        fun `should return maximum of 100 postal vote applications`() {
            // Given
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = 200)
        }

        @Test
        fun `system does not have requested number of records in the DB`() {
            // Given validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 100, pageSizeRequested = null)
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 10, pageSizeRequested = 100)
        }

        @Test
        fun `system does not have any records`() {
            validateFetchPostalVoteApplications(numberOfRecordsToBeReturned = 0, pageSizeRequested = 100)
        }
    }

    private fun validateFetchPostalVoteApplications(numberOfRecordsToBeReturned: Int, pageSizeRequested: Int?) {
        // Given

        if (pageSizeRequested == null) {
            given(apiProperties.defaultPageSize).willReturn(defaultPageSize)
        }

        val savedApplications =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj {
                buildPostalVoteApplication(applicationId = it.toString())
            }.toList()
        val mockPostalVotes =
            IntStream.rangeClosed(1, numberOfRecordsToBeReturned).mapToObj { mock<PostalVote>() }.toList()

        given(
            postalVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(pageSizeRequested ?: defaultPageSize)
            )
        ).willReturn(savedApplications)
        given { postalVoteMapper.mapFromEntities(savedApplications) }.willReturn(mockPostalVotes)

        val postalVoteAcceptedResponse =
            postalVoteApplicationService.getPostalVoteApplications(pageSize = pageSizeRequested)

        assertThat(postalVoteAcceptedResponse.pageSize).isEqualTo(numberOfRecordsToBeReturned)
        // the attribute name 'proxyVotes' to postalVotes, awaiting final spec from EMS
        assertThat(postalVoteAcceptedResponse.proxyVotes).isEqualTo(mockPostalVotes)
    }
}
