package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.exception.RegisterCheckSaveDataIntegrityException
import uk.gov.dluhc.emsintegrationapi.messaging.mapper.InitiateRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.service.RegisterCheckService
import uk.gov.dluhc.messagingsupport.MessageListener
import uk.gov.dluhc.registercheckerapi.messaging.models.InitiateRegisterCheckMessage

private val logger = KotlinLogging.logger { }

/**
 * Implementation of [MessageListener] to handle [InitiateRegisterCheckMessage] messages
 */
@Component
class InitiateRegisterCheckMessageListener(
    private val registerCheckService: RegisterCheckService,
    private val mapper: InitiateRegisterCheckMapper
) :
    MessageListener<InitiateRegisterCheckMessage> {

    @SqsListener("\${sqs.initiate-applicant-register-check-queue-name}")
    override fun handleMessage(@Valid @Payload payload: InitiateRegisterCheckMessage) {
        with(payload) {
            logger.info {
                "New InitiateRegisterCheckMessage received with " +
                    "sourceReference: $sourceReference and " +
                    "sourceCorrelationId: $sourceCorrelationId"
            }

            val pendingRegisterCheckDto = mapper.initiateCheckMessageToPendingRegisterCheckDto(this)
            try {
                registerCheckService.save(pendingRegisterCheckDto)
            } catch (ex: DataIntegrityViolationException) {
                val registerCheck =
                    registerCheckService.getRegisterCheckOrNull(pendingRegisterCheckDto.sourceCorrelationId)
                if (registerCheck !== null) {
                    logger.warn(
                        "Attempted to initiate register check with source correlation ID [${pendingRegisterCheckDto.sourceCorrelationId}] for application [${pendingRegisterCheckDto.sourceReference}]. Request failed due to duplicate register check found."
                    )

                    return
                }

                throw RegisterCheckSaveDataIntegrityException(pendingRegisterCheckDto.sourceCorrelationId).initCause(ex)
            }
        }
    }
}
