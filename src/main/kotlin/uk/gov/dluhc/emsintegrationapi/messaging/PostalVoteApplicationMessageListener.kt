package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessPostalVoteApplicationMessageService
import javax.validation.Valid

private val logger = KotlinLogging.logger { }

@Component
class PostalVoteApplicationMessageListener(private val processPostalVoteApplicationMessageService: ProcessPostalVoteApplicationMessageService) {
    @SqsListener("\${sqs.postal-application-queue-name}")
    fun handleMessage(@Valid @Payload postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        with(postalVoteApplicationMessage) {
            logger.info("Postal Vote Application Message received with an application id = ${applicationDetails.id}")
            processPostalVoteApplicationMessageService.process(this)
        }
    }
}
