package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails.EmsStatus.*
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source.*
import java.util.*

private val logger = KotlinLogging.logger { }

private val successOrFailure = listOf(SUCCESS, FAILURE)

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
        postalVoteApplicationRepository.findById(applicationId)
            .let { it ->
                it.map {
                    val applicationDetails = it.applicationDetails
                    if(Optional.ofNullable(applicationDetails.emsStatus).isEmpty) {
                        throw IntegrationDataRemovalFailedException(applicationId, POSTAL)

                    } else {
                        logger.info { "Deleting $POSTAL application ems data with id = ${it.applicationId}" }
                        postalVoteApplicationRepository.delete(it)
                    }
                }.orElseThrow { ApplicationNotFoundException(applicationId, ApplicationType.POSTAL) }
            }
    }

    fun processProxyApplicationRemoval(applicationId: String) {
        proxyVoteApplicationRepository.findById(applicationId)
            .let { it ->
                it.map {
                    val applicationDetails = it.applicationDetails
                    if(Optional.ofNullable(applicationDetails.emsStatus).isEmpty) {
                        throw IntegrationDataRemovalFailedException(applicationId, PROXY)

                    } else {
                        logger.info { "Deleting $PROXY application ems data with id = ${it.applicationId}" }
                        proxyVoteApplicationRepository.delete(it)
                    }
                }.orElseThrow { ApplicationNotFoundException(applicationId, ApplicationType.PROXY) }
            }
    }
}