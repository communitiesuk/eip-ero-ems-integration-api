package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteApplications

private val logger = KotlinLogging.logger { }

@Service
class PostalVoteApplicationService(
    private val apiProperties: ApiProperties,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteMapper: PostalVoteMapper,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
    private val retrieveGssCodeService: RetrieveGssCodeService
) {
    @Transactional(readOnly = true)
    fun getPostalVoteApplications(certificateSerialNumber: String, pageSize: Int?): PostalVoteApplications {
        val gssCodes = getGssCodes(certificateSerialNumber)
        val numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        logger.info("Fetching $pageSize applications from DB for Serial No=$certificateSerialNumber and gss codes = $gssCodes")
        val postalApplicationsList =
            postalVoteApplicationRepository.findByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                gssCodes,
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch)
            )
        val actualPageSize = postalApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return PostalVoteApplications(actualPageSize, postalVoteMapper.mapFromEntities(postalApplicationsList))
    }

    @Transactional
    fun confirmReceipt(
        certificateSerialNumber: String,
        postalVoteApplicationId: String,
        request: EMSApplicationResponse
    ) {
        val gssCodes = getGssCodes(certificateSerialNumber)
        logger.info("Updating the postal vote application with the id $postalVoteApplicationId with status ${RecordStatus.DELETED}")
        postalVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
            postalVoteApplicationId,
            gssCodes
        )
            ?.let { postalVoteApplication ->
                if (postalVoteApplication.status != RecordStatus.DELETED) {
                    doReceipt(request, postalVoteApplication)
                } else {
                    logger.warn {
                        "The status of the postal vote application with id $postalVoteApplicationId" +
                            " is already ${RecordStatus.DELETED}, so ignoring the request "
                    }
                }
            } ?: throw ApplicationNotFoundException(
            applicationId = postalVoteApplicationId,
            applicationType = ApplicationType.POSTAL
        )
    }

    private fun doReceipt(request: EMSApplicationResponse, postalVoteApplication: PostalVoteApplication) {
        with(postalVoteApplication) {
            status = RecordStatus.DELETED
            updatedBy = SourceSystem.EMS
            with(applicationDetails) {
                emsStatus = when (request.status) {
                    EMSApplicationStatus.SUCCESS -> ApplicationDetails.EmsStatus.SUCCESS
                    EMSApplicationStatus.FAILURE -> ApplicationDetails.EmsStatus.FAILURE
                }
                emsMessage = request.message
                emsDetails = request.details
            }
        }

        postalVoteApplicationRepository.saveAndFlush(postalVoteApplication)
        sendMessage(request, postalVoteApplication.applicationId)

        logger.info { "Confirmation ${request.status} message sent to the postal vote application for ${postalVoteApplication.applicationId}" }
    }

    private fun sendMessage(request: EMSApplicationResponse, postalVoteApplicationId: String) {
        messageSender.send(
            EmsConfirmedReceiptMessage(
                id = postalVoteApplicationId,
                status = request.status,
                message = request.message,
                details = request.details
            ),
            DELETED_POSTAL_APPLICATION_QUEUE
        )
    }

    private fun getGssCodes(certificateSerialNumber: String): List<String> {
        logger.info { "Fetching GSS Codes for $certificateSerialNumber" }
        return retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber)
    }
}
