package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteApplications

private val logger = KotlinLogging.logger { }

@Service
class ProxyVoteApplicationService(
    private val apiProperties: ApiProperties,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteMapper: ProxyVoteMapper,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
    private val retrieveGssCodeService: RetrieveGssCodeService
) {
    @Transactional(readOnly = true)
    fun getProxyVoteApplications(certificateSerialNumber: String, pageSize: Int?): ProxyVoteApplications {
        logger.info { "Proxy Service fetching GSS Codes for $certificateSerialNumber" }
        val gssCodes = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber)
        val numberOfRecordsToFetch = pageSize ?: apiProperties.defaultPageSize
        logger.info("Fetching $pageSize proxy vote applications from DB for Serial No=$certificateSerialNumber and gss codes = $gssCodes")
        val proxyApplicationsList =
            proxyVoteApplicationRepository.findByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
                gssCodes,
                RecordStatus.RECEIVED,
                Pageable.ofSize(numberOfRecordsToFetch)
            )
        val actualPageSize = proxyApplicationsList.size
        logger.info("The actual number of records fetched is $actualPageSize")
        return ProxyVoteApplications(actualPageSize, proxyVoteMapper.mapFromEntities(proxyApplicationsList))
    }

    @Transactional
    fun confirmReceipt(
        certificateSerialNumber: String,
        proxyVoteApplicationId: String,
    ) {
        val gssCodes = getGssCodes(certificateSerialNumber)
        logger.info("Updating the proxy vote application with the id $proxyVoteApplicationId with status ${RecordStatus.DELETED}")
        proxyVoteApplicationRepository.findByApplicationIdAndApplicationDetailsGssCodeIn(
            proxyVoteApplicationId,
            gssCodes
        )
            ?.let { proxyVoteApplication ->
                if (proxyVoteApplication.status != RecordStatus.DELETED) {
                    proxyVoteApplication.status = RecordStatus.DELETED
                    proxyVoteApplication.updatedBy = SourceSystem.EMS
                    proxyVoteApplicationRepository.saveAndFlush(proxyVoteApplication)
                    messageSender.send(
                        EmsConfirmedReceiptMessage(proxyVoteApplicationId, EMSApplicationStatus.SUCCESS),
                        QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
                    )
                    logger.info { "Confirmation message sent to the proxy vote application for $proxyVoteApplicationId" }
                } else {
                    logger.warn {
                        "The status of the proxy vote application with id $proxyVoteApplicationId" +
                            " is already ${RecordStatus.DELETED}, so ignoring the request "
                    }
                }
            } ?: throw ApplicationNotFoundException(
            applicationId = proxyVoteApplicationId,
            applicationType = ApplicationType.PROXY
        )
    }

    private fun getGssCodes(certificateSerialNumber: String): List<String> {
        logger.info { "Fetching GSS Codes for $certificateSerialNumber" }
        return retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber)
    }
}
