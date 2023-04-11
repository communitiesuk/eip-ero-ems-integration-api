package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse

private val logger = KotlinLogging.logger { }

@Service
class PostalVoteApplicationService(
    private val apiProperties: ApiProperties,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteMapper: PostalVoteMapper,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
) {
    @Transactional(readOnly = true)
    fun getPostalVoteApplications(certificateSerialNumber: String, pageSize: Int?): PostalVoteAcceptedResponse {
        val numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        // TODO - Serial number validation
        logger.info("Fetching $pageSize applications from DB for Serial No=$certificateSerialNumber")
        val postalApplicationsList =
            postalVoteApplicationRepository.findByStatusOrderByDateCreated(
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch)
            )
        val actualPageSize = postalApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return PostalVoteAcceptedResponse(actualPageSize, postalVoteMapper.mapFromEntities(postalApplicationsList))
    }

    @Transactional
    fun confirmReceipt(
        postalVoteApplicationId: String,
    ) {
        logger.info("Updating the postal vote application with the id $postalVoteApplicationId with status ${RecordStatus.DELETED}")
        postalVoteApplicationRepository.findById(postalVoteApplicationId).map { postalVoteApplication ->
            if (postalVoteApplication.status != RecordStatus.DELETED) {
                postalVoteApplication.status = RecordStatus.DELETED
                postalVoteApplication.updatedBy = SourceSystem.EMS
                postalVoteApplicationRepository.saveAndFlush(postalVoteApplication)
                messageSender.send(
                    EmsConfirmedReceiptMessage(postalVoteApplicationId),
                    DELETED_POSTAL_APPLICATION_QUEUE
                )
                logger.info { "Confirmation message sent to the postal vote application for $postalVoteApplicationId" }
            } else {
                logger.warn {
                    "The status of the postal vote application with id $postalVoteApplicationId" +
                        " is already ${RecordStatus.DELETED}, so ignoring the request "
                }
            }
        }.orElseThrow {
            ApplicationNotFoundException(
                applicationId = postalVoteApplicationId,
                applicationType = ApplicationType.POSTAL
            )
        }
    }
}
