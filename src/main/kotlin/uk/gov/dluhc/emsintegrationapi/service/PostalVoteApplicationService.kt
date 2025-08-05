package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteApplications
import java.time.Clock

private val logger = KotlinLogging.logger { }

@Service
class PostalVoteApplicationService(
    // IntelliJ cannot resolve the clock bean as it's determined at runtime. See https://stackoverflow.com/questions/21323309/intellij-idea-shows-errors-when-using-springs-autowired-annotation
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock,
    private val apiProperties: ApiProperties,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteMapper: PostalVoteMapper,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val retrieveIsHoldEnabledForEroService: RetrieveIsHoldEnabledForEroService,
) : AbstractApplicationService(
    clock,
    QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE,
    apiProperties,
    retrieveGssCodeService,
    retrieveIsHoldEnabledForEroService,
    messageSender,
) {
    @Transactional(readOnly = true)
    fun getPostalVoteApplications(
        certificateSerialNumber: String,
        pageSize: Int?,
    ): PostalVoteApplications {
        if (shouldHoldApplicationsForEro(certificateSerialNumber)) {
            logger.info("No postal records fetched for $certificateSerialNumber, as hold is enabled for ERO and after holding pool threshold date")
            return PostalVoteApplications(0, emptyList())
        }

        val gssCodes = getGssCodes(certificateSerialNumber)
        var numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        logger.info("Fetching $pageSize applications from DB for Serial No=$certificateSerialNumber and gss codes = $gssCodes")
        if (numberOfRecordsToFetch > apiProperties.forceMaxPageSize) {
            logger.warn(
                "Force setting number of records to fetch to ${apiProperties.forceMaxPageSize}, ignoring requested record count of $numberOfRecordsToFetch",
            )
            numberOfRecordsToFetch = apiProperties.forceMaxPageSize
        }
        val postalApplicationIds =
            postalVoteApplicationRepository.findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                gssCodes,
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch),
            )
        val postalApplications = postalVoteApplicationRepository.findByApplicationIdIn(postalApplicationIds)
        val postalApplicationsList =
            postalApplicationIds.map { id -> postalApplications.find { it.applicationId == id }!! }

        val actualPageSize = postalApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return PostalVoteApplications(actualPageSize, postalVoteMapper.mapFromEntities(postalApplicationsList))
    }

    @Transactional
    fun confirmReceipt(
        certificateSerialNumber: String,
        postalVoteApplicationId: String,
        request: EMSApplicationResponse,
    ) {
        val gssCodes = getGssCodes(certificateSerialNumber)
        logger.info("Updating the postal vote application with the id $postalVoteApplicationId with status ${RecordStatus.DELETED}")
        postalVoteApplicationRepository
            .findByApplicationIdAndApplicationDetailsGssCodeIn(
                postalVoteApplicationId,
                gssCodes,
            )?.let { postalVoteApplication ->
                if (postalVoteApplication.status != RecordStatus.DELETED) {
                    doConfirmedReceiptApplication(request, postalVoteApplication)
                } else {
                    logger.warn {
                        "The status of the postal vote application with id $postalVoteApplicationId" +
                            " is already ${RecordStatus.DELETED}, so ignoring the request "
                    }
                }
            } ?: throw ApplicationNotFoundException(
            applicationId = postalVoteApplicationId,
            applicationType = ApplicationType.POSTAL,
        )
    }

    private fun doConfirmedReceiptApplication(
        request: EMSApplicationResponse,
        postalVoteApplication: PostalVoteApplication,
    ) {
        with(postalVoteApplication) {
            status = RecordStatus.DELETED
            updatedBy = SourceSystem.EMS
            doConfirmedReceiptApplicationStatus(request, postalVoteApplication.applicationDetails)
        }

        sendMessage(request, postalVoteApplication.applicationId, postalVoteApplication.isFromApplicationsApi)

        logger.info {
            "Confirmation ${request.status} message sent to the postal vote application for ${postalVoteApplication.applicationId}"
        }
    }
}
