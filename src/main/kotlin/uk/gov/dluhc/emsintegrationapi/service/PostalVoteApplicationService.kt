package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse

private val logger = KotlinLogging.logger { }

@Service
class PostalVoteApplicationService(
    private val apiProperties: ApiProperties,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteMapper: PostalVoteMapper
) {
    @Transactional(readOnly = true)
    fun getPostalVoteApplications(pageSize: Int?): PostalVoteAcceptedResponse {
        val numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        logger.info("Fetching $pageSize applications from DB")
        val postalApplicationsList =
            postalVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch)
            )
        val actualPageSize = postalApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return PostalVoteAcceptedResponse(actualPageSize, postalVoteMapper.mapFromEntities(postalApplicationsList))
    }
}
