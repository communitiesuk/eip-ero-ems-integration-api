package uk.gov.dluhc.emsintegrationapi.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage

@ExtendWith(MockitoExtension::class)
internal class ProcessPostalVoteApplicationMessageServiceTest {

    @Mock
    private lateinit var postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper

    @Mock
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @InjectMocks
    private lateinit var processPostalVoteApplicationMessageService: ProcessPostalVoteApplicationMessageService

    @Test
    fun `should save postal vote application`() {
        val postalVoteApplicationMessage = buildPostalVoteApplicationMessage()

        val mappedEntity = buildPostalVoteApplication(applicationId = postalVoteApplicationMessage.approvalDetails.id)
        given(postalVoteApplicationMessageMapper.mapToEntity(postalVoteApplicationMessage)).willReturn(mappedEntity)

        processPostalVoteApplicationMessageService.process(postalVoteApplicationMessage)

        verify(postalVoteApplicationMessageMapper).mapToEntity(postalVoteApplicationMessage)
        verify(postalVoteApplicationRepository).save(mappedEntity)
    }
}
