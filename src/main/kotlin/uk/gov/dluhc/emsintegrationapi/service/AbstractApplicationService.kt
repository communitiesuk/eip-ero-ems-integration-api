package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import java.time.Clock
import java.time.Instant

private val logger = KotlinLogging.logger { }

abstract class AbstractApplicationService(
    private val clock: Clock,
    private val apiProperties: ApiProperties,
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val retreiveIsHoldEnabledForEroService: RetrieveIsHoldEnabledForEroService,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
) {
    protected fun getGssCodes(certificateSerialNumber: String): List<String> {
        logger.info { "Fetching GSS Codes for $certificateSerialNumber" }
        return retrieveGssCodeService.getGssCodesFromCertificateSerial(certificateSerialNumber)
    }

    protected fun shouldHoldApplicationsForEro(
        certificateSerialNumber: String,
    ): Boolean {
        logger.info { "Determining if applications should be held, due to convergence, for ERO with certificate serial number $certificateSerialNumber" }
        val thresholdDate = apiProperties.holdingPoolThresholdDate
        if (Instant.now(clock).isBefore(thresholdDate)) {
            return false
        }
        return retreiveIsHoldEnabledForEroService.getIsHoldEnabled(certificateSerialNumber)
    }

    protected fun doConfirmedReceiptApplicationStatus(
        request: EMSApplicationResponse,
        applicationDetails: ApplicationDetails,
    ) {
        with(applicationDetails) {
            emsStatus =
                when (request.status) {
                    EMSApplicationStatus.SUCCESS -> ApplicationDetails.EmsStatus.SUCCESS
                    EMSApplicationStatus.FAILURE -> ApplicationDetails.EmsStatus.FAILURE
                }
            emsMessage = request.message
            emsDetails = request.details
        }
    }

    protected fun sendMessage(
        request: EMSApplicationResponse,
        applicationId: String,
    ) {
        messageSender.send(
            EmsConfirmedReceiptMessage(
                id = applicationId,
                status =
                when (request.status) {
                    EMSApplicationStatus.SUCCESS -> EmsConfirmedReceiptMessage.Status.SUCCESS
                    EMSApplicationStatus.FAILURE -> EmsConfirmedReceiptMessage.Status.FAILURE
                },
                message = request.message,
                details = request.details,
            ),
            QueueConfiguration.QueueName.EMS_APPLICATION_PROCESSED_QUEUE,
        )
    }
}
