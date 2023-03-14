package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration

@Component
class MessageSender<T : Any>(
    private val queueConfiguration: QueueConfiguration,
    private val queueMessagingTemplate: QueueMessagingTemplate
) {

    fun submit(message: T, queueName: QueueConfiguration.QueueName) =
        queueMessagingTemplate.convertAndSend(queueConfiguration.getQueueNameFrom(queueName), message)

    fun submit(messages: List<T>, queueName: QueueConfiguration.QueueName) =
        messages.forEach { queueMessagingTemplate.convertAndSend(queueConfiguration.getQueueNameFrom(queueName), it) }
}
