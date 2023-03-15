package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage

private val logger = KotlinLogging.logger { }

@Service
class ProcessPostalVoteApplicationMessageService(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper
) {
    @Transactional
    fun process(postalVoteApplicationMessage: PostalVoteApplicationMessage) {

        with(postalVoteApplicationMessage) {
            logger.info { "Processing postal vote application with id = ${approvalDetails.id}" }
            val postalVoteApplicationEntity =
                postalVoteApplicationMessageMapper.mapToEntity(postalVoteApplicationMessage)
            logger.debug { "Successfully mapped the message to entity for application = ${approvalDetails.id}" }
            postalVoteApplicationRepository.save(postalVoteApplicationEntity!!)
            logger.info { "Successfully saved the application id  = ${approvalDetails.id}" }
        }
    }
}
