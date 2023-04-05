package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteAcceptedResponse

private val logger = KotlinLogging.logger { }

@Service
class ProxyVoteApplicationService(
    private val apiProperties: ApiProperties,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteMapper: ProxyVoteMapper
) {
    @Transactional(readOnly = true)
    fun getProxyVoteApplications(certificateSerialNumber: String, pageSize: Int?): ProxyVoteAcceptedResponse {
        val numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        // TODO - Serial number validation
        logger.info("Fetching $pageSize applications from DB for Serial No=$certificateSerialNumber")
        val proxyApplicationsList =
            proxyVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch)
            )
        val actualPageSize = proxyApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return ProxyVoteAcceptedResponse(actualPageSize, proxyVoteMapper.mapFromEntities(proxyApplicationsList))
    }
}
