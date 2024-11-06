package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails.EmsStatus.*
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source.*

private val logger = KotlinLogging.logger { }

@Service
class ProcessIntegrationDataRemovalMessageService(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
) {
    @Transactional
    fun process(removeEmsDataMessage: RemoveVoterApplicationEmsDataMessage) {
        with(removeEmsDataMessage) {
            logger.info { "Processing postal vote application with id = ${removeEmsDataMessage.applicationId}" }
            when(removeEmsDataMessage.source) {
                POSTAL -> processPostalApplicationRemoval(removeEmsDataMessage.applicationId)
                PROXY -> processProxyApplicationRemoval(removeEmsDataMessage.applicationId)
            }
        }
    }

    fun processPostalApplicationRemoval(applicationId: String) {
        postalVoteApplicationRepository.findByApplicationIdIn(listOf(applicationId))
            ?.let { applicationList ->
                if (applicationList.isNotEmpty()) {
                    val application = applicationList.get(0)
                    val applicationDetails = application.applicationDetails
                    if(applicationDetails === null) {
                        throw IntegrationDataRemovalFailedException(applicationId, POSTAL)
                    } else if (applicationDetails.emsStatus == SUCCESS
                        || applicationDetails.emsStatus == FAILURE
                    ){
                        postalVoteApplicationRepository.delete(application)
                    } else {
                        logger.warn {
                            "The status of the $POSTAL vote application with id ${application.applicationId}" +
                                    " is not $SUCCESS or $FAILURE, so ignoring the request "
                        }
                    }
                }
            }
    }

    fun processProxyApplicationRemoval(applicationId: String) {
        proxyVoteApplicationRepository.findByApplicationIdIn(listOf(applicationId))
            ?.let { applicationList ->
                if (applicationList.isNotEmpty()) {
                    val application = applicationList.get(0)
                    val applicationDetails = application.applicationDetails
                    if(applicationDetails === null) {
                        throw IntegrationDataRemovalFailedException(applicationId, PROXY)
                    } else if (applicationDetails.emsStatus == SUCCESS
                        || applicationDetails.emsStatus == FAILURE
                    ){
                        proxyVoteApplicationRepository.delete(application)
                    } else {
                        logger.warn {
                            "The status of the $PROXY vote application with id ${application.applicationId}" +
                                    " is not $SUCCESS or $FAILURE, so ignoring the request "
                        }
                    }
                }
            }
    }
}