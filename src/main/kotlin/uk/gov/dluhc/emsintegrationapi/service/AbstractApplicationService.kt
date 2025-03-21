package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus

private val logger = KotlinLogging.logger { }

abstract class AbstractApplicationService(
    private val queueName: QueueConfiguration.QueueName,
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val messageSender: MessageSender<EmsConfirmedReceiptMessage>,
) {
    protected fun getGssCodes(certificateSerialNumber: String): List<String> {
        logger.info { "Fetching GSS Codes for $certificateSerialNumber" }
        return retrieveGssCodeService.getGssCodesFromCertificateSerial(certificateSerialNumber)
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
        isFromApplicationsApi: Boolean? = null,
    ) {
        val targetQueueName =
            if (isFromApplicationsApi == true) QueueConfiguration.QueueName.EMS_APPLICATION_PROCESSED_QUEUE else queueName
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
            targetQueueName,
        )
    }
}
