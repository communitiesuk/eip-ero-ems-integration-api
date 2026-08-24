package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessPostalVoteApplicationMessageService
import uk.gov.dluhc.messagingsupport.MessageListener

private val logger = KotlinLogging.logger { }

@Component
class PostalVoteApplicationMessageListener(private val processPostalVoteApplicationMessageService: ProcessPostalVoteApplicationMessageService) :
    MessageListener<PostalVoteApplicationMessage> {
    @SqsListener("\${sqs.postal-application-queue-name}")
    override fun handleMessage(@Valid @Payload payload: PostalVoteApplicationMessage) {
        with(payload) {
            logger.info { "Postal Vote Application Message received with an application id = ${applicationDetails.id}" }
            processPostalVoteApplicationMessageService.process(this)
        }
    }
}
