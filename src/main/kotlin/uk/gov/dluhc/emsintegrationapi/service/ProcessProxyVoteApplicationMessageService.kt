package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage

private val logger = KotlinLogging.logger { }

@Service
class ProcessProxyVoteApplicationMessageService(
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper
) {
    @Transactional
    fun process(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {

        with(proxyVoteApplicationMessage) {
            logger.info { "Processing proxy vote application with id = ${applicationDetails.id}" }
            val proxyVoteApplicationEntity =
                proxyVoteApplicationMessageMapper.mapToEntity(this)
            logger.debug { "Successfully mapped the proxy application message to entity for application = ${applicationDetails.id}" }
            proxyVoteApplicationRepository.save(proxyVoteApplicationEntity)
            logger.info { "Successfully saved the proxy application with the id  = ${applicationDetails.id}" }
        }
    }
}
