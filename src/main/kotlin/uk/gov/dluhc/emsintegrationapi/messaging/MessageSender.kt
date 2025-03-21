package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration

@Component
class MessageSender<T : Any>(
    private val queueConfiguration: QueueConfiguration,
    private val queueMessagingTemplate: SqsTemplate,
) {
    fun send(
        message: T,
        queueName: QueueConfiguration.QueueName,
    ) = queueMessagingTemplate.send(
        queueConfiguration.getQueueNameFrom(queueName),
        message,
    )
}
