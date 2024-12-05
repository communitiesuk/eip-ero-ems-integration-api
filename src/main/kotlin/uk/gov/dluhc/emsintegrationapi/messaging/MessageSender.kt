package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.messaging.Message
import org.springframework.messaging.core.MessagePostProcessor
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration

@Component
class MessageSender<T : Any>(
    private val queueConfiguration: QueueConfiguration,
    private val queueMessagingTemplate: SqsTemplate
) {
    fun send(message: T, queueName: QueueConfiguration.QueueName) =
        queueMessagingTemplate.send(
            queueConfiguration.getQueueNameFrom(queueName),
            queueMessagingTemplate.convertToMessage(message)
        )

    /**
     * Extension function on [QueueMessagingTemplate] to allow invocation of it's protected `doConvert` method to
     * get convert the payload into a [Message] with all the default headers etc.
     */
    private fun SqsTemplate.convertToMessage(payload: T): Message<T> =
        javaClass.getDeclaredMethod("doConvert", Any::class.java, Map::class.java, MessagePostProcessor::class.java)
            .let {
                it.isAccessible = true
                it.invoke(this, payload, null, null) as Message<T>
            }
}
