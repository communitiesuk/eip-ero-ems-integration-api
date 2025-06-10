package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.mapper.InitiateRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.service.RegisterCheckService
import uk.gov.dluhc.messagingsupport.MessageListener
import uk.gov.dluhc.registercheckerapi.messaging.models.InitiateRegisterCheckForwardingMessage

private val logger = KotlinLogging.logger { }

/**
 * Implementation of [MessageListener] to handle [InitiateRegisterCheckForwardingMessage] messages
 */
@Component
class InitiateRegisterCheckMessageListener(
    private val registerCheckService: RegisterCheckService,
    private val mapper: InitiateRegisterCheckMapper
) :
    MessageListener<InitiateRegisterCheckForwardingMessage> {

    @SqsListener("\${sqs.initiate-applicant-register-check-queue-name}")
    override fun handleMessage(@Valid @Payload payload: InitiateRegisterCheckForwardingMessage) {
        with(payload) {
            logger.info {
                "New InitiateRegisterCheckMessage received with " +
                    "sourceReference: $sourceReference and " +
                    "sourceCorrelationId: $sourceCorrelationId and " +
                    "correlationId: $correlationId"
            }

            val pendingRegisterCheckDto = mapper.initiateCheckForwardingMessageToPendingRegisterCheckDto(this)
            registerCheckService.save(pendingRegisterCheckDto)
        }
    }
}
