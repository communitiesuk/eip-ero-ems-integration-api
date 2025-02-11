package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.service.PendingRegisterCheckArchiveService
import uk.gov.dluhc.messagingsupport.MessageListener
import uk.gov.dluhc.registercheckerapi.messaging.models.PendingRegisterCheckArchiveMessage

private val logger = KotlinLogging.logger { }

/**
 * Implementation of [MessageListener] to handle [PendingRegisterCheckArchiveMessage] messages
 */
@Component
class PendingRegisterCheckArchiveDataMessageListener(
    private val pendingRegisterCheckArchiveService: PendingRegisterCheckArchiveService,
) : MessageListener<PendingRegisterCheckArchiveMessage> {
    @SqsListener("\${sqs.pending-register-check-archive-queue-name}")
    override fun handleMessage(
        @Valid @Payload payload: PendingRegisterCheckArchiveMessage,
    ) {
        with(payload) {
            logger.info {
                "New PendingRegisterCheckArchiveMessage received with " +
                    "correlationId: $correlationId"
            }
            pendingRegisterCheckArchiveService.archiveIfStatusIsPending(correlationId)
        }
    }
}
