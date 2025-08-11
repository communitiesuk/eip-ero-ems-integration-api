package uk.gov.dluhc.emsintegrationapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import uk.gov.dluhc.messagingsupport.MessageQueue
import uk.gov.dluhc.messagingsupport.MessagingConfigurationHelper
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResultMessage

@Configuration
class MessagingConfiguration {

    @Value("\${sqs.register-check-result-response-queue-name}")
    private lateinit var registerCheckResultResponseQueueName: String

    @Bean(name = ["registerCheckResultResponseQueue"])
    fun registerCheckResultResponseQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(registerCheckResultResponseQueueName, sqsTemplate)

    @Bean
    fun sqsMessagingMessageConverter(
        objectMapper: ObjectMapper,
    ) = MessagingConfigurationHelper.sqsMessagingMessageConverter(objectMapper)

    @Bean
    fun defaultSqsListenerContainerFactory(
        objectMapper: ObjectMapper,
        sqsAsyncClient: SqsAsyncClient,
        sqsMessagingMessageConverter: SqsMessagingMessageConverter,
    ) = MessagingConfigurationHelper.defaultSqsListenerContainerFactory(
        sqsAsyncClient = sqsAsyncClient,
        sqsMessagingMessageConverter = sqsMessagingMessageConverter,
        maximumNumberOfConcurrentMessages = null,
    )
}
