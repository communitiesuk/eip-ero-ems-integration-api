package uk.gov.dluhc.emsintegrationapi.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.messaging.listener.annotation.SqsListener
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import javax.validation.Valid

private val logger = KotlinLogging.logger { }

@Component
class PostalVoteApplicationMessageListener(private val objectMapper: ObjectMapper) {
    @SqsListener("\${sqs.postal-application-queue-name}")
    fun handleMessage(@Valid @Payload postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        logger.info("Message received ${objectMapper.writeValueAsString(postalVoteApplicationMessage)}")
    }
}
