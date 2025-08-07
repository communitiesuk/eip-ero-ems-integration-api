package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteApplications
import java.time.Clock

private val logger = KotlinLogging.logger { }

@Service
class ProxyVoteApplicationService(
    // IntelliJ cannot resolve the clock bean as it's determined at runtime. See https://stackoverflow.com/questions/21323309/intellij-idea-shows-errors-when-using-springs-autowired-annotation
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock,
    private val apiProperties: ApiProperties,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteMapper: ProxyVoteMapper,
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
    private val retrieveIsHoldEnabledForEroService: RetrieveIsHoldEnabledForEroService
) : AbstractApplicationService(
    clock,
    QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE,
    apiProperties,
    retrieveGssCodeService,
    retrieveIsHoldEnabledForEroService,
    messageSender
) {
    @Transactional(readOnly = true)
    fun getProxyVoteApplications(
        certificateSerialNumber: String,
        pageSize: Int?,
    ): ProxyVoteApplications {
        if (shouldHoldApplicationsForEro(certificateSerialNumber)) {
            logger.info("No proxy records fetched for $certificateSerialNumber, as hold is enabled for ERO and after holding pool threshold date")
            return ProxyVoteApplications(0, emptyList())
        }
        logger.info { "Proxy Service fetching GSS Codes for $certificateSerialNumber" }
        val gssCodes = retrieveGssCodeService.getGssCodesFromCertificateSerial(certificateSerialNumber)
        var numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        logger.info("Fetching $pageSize proxy vote applications from DB for Serial No=$certificateSerialNumber and gss codes = $gssCodes")
        if (numberOfRecordsToFetch > apiProperties.forceMaxPageSize) {
            logger.warn(
                "Force setting number of records to fetch to ${apiProperties.forceMaxPageSize}, ignoring requested record count of $numberOfRecordsToFetch",
            )
            numberOfRecordsToFetch = apiProperties.forceMaxPageSize
        }
        val proxyApplicationIds =
            proxyVoteApplicationRepository.findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                gssCodes,
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch),
            )
        val proxyApplications = proxyVoteApplicationRepository.findByApplicationIdIn(proxyApplicationIds)
        val proxyApplicationsList =
            proxyApplicationIds.map { id -> proxyApplications.find { it.applicationId == id }!! }
        val actualPageSize = proxyApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return ProxyVoteApplications(actualPageSize, proxyVoteMapper.mapFromEntities(proxyApplicationsList))
    }

    @Transactional
    fun confirmReceipt(
        certificateSerialNumber: String,
        proxyVoteApplicationId: String,
        request: EMSApplicationResponse,
    ) {
        val gssCodes = getGssCodes(certificateSerialNumber)

        logger.info("Updating the proxy vote application with the id $proxyVoteApplicationId with status ${RecordStatus.DELETED}")
        proxyVoteApplicationRepository
            .findByApplicationIdAndApplicationDetailsGssCodeIn(
                proxyVoteApplicationId,
                gssCodes,
            )?.let { proxyVoteApplication ->
                if (proxyVoteApplication.status != RecordStatus.DELETED) {
                    doConfirmedReceiptApplication(request, proxyVoteApplication)
                } else {
                    logger.warn {
                        "The status of the proxy vote application with id $proxyVoteApplicationId" +
                            " is already ${RecordStatus.DELETED}, so ignoring the request "
                    }
                }
            } ?: throw ApplicationNotFoundException(
            applicationId = proxyVoteApplicationId,
            applicationType = ApplicationType.PROXY,
        )
    }

    private fun doConfirmedReceiptApplication(
        request: EMSApplicationResponse,
        proxyVoteApplication: ProxyVoteApplication,
    ) {
        with(proxyVoteApplication) {
            status = RecordStatus.DELETED
            updatedBy = SourceSystem.EMS
            doConfirmedReceiptApplicationStatus(request, proxyVoteApplication.applicationDetails)
        }

        sendMessage(request, proxyVoteApplication.applicationId, proxyVoteApplication.isFromApplicationsApi)

        logger.info {
            "Confirmation ${request.status} message sent to the proxy vote application for ${proxyVoteApplication.applicationId}"
        }
    }
}
