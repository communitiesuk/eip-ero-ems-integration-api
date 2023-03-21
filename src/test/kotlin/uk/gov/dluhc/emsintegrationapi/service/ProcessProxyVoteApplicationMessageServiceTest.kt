package uk.gov.dluhc.emsintegrationapi.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto

@ExtendWith(MockitoExtension::class)
internal class ProcessProxyVoteApplicationMessageServiceTest {

    @Mock
    private lateinit var proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper

    @Mock
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @InjectMocks
    private lateinit var processProxyVoteApplicationMessageService: ProcessProxyVoteApplicationMessageService

    @Test
    fun `should save a proxy vote application`() {
        val proxyVoteApplicationMessage = buildProxyVoteApplicationMessageDto()

        val mappedEntity = buildProxyVoteApplication(applicationId = proxyVoteApplicationMessage.approvalDetails.id)
        given(proxyVoteApplicationMessageMapper.mapToEntity(proxyVoteApplicationMessage)).willReturn(mappedEntity)

        processProxyVoteApplicationMessageService.process(proxyVoteApplicationMessage)

        verify(proxyVoteApplicationMessageMapper).mapToEntity(proxyVoteApplicationMessage)
        verify(proxyVoteApplicationRepository).save(mappedEntity)
    }
}
