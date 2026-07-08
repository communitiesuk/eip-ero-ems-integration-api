package uk.gov.dluhc.emsintegrationapi.config

import io.awspring.cloud.sqs.config.SqsListenerConfigurer
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import tools.jackson.databind.json.JsonMapper
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
        jsonMapper: JsonMapper,
    ) = MessagingConfigurationHelper.sqsMessagingMessageConverter(jsonMapper)

    @Bean
    fun defaultSqsListenerContainerFactory(
        jsonMapper: JsonMapper,
        sqsAsyncClient: SqsAsyncClient,
        sqsMessagingMessageConverter: SqsMessagingMessageConverter,
    ) = MessagingConfigurationHelper.defaultSqsListenerContainerFactory(
        sqsAsyncClient = sqsAsyncClient,
        sqsMessagingMessageConverter = sqsMessagingMessageConverter,
        maximumNumberOfConcurrentMessages = null,
    )

    @Bean
    fun sqsListenerConfigurer(localValidatorFactoryBean: LocalValidatorFactoryBean): SqsListenerConfigurer =
        SqsListenerConfigurer { registrar ->
            registrar.setMethodPayloadTypeInferrer(null)
            registrar.setValidator(localValidatorFactoryBean)
        }

    @Bean
    fun localValidatorFactoryBean(): LocalValidatorFactoryBean =
        LocalValidatorFactoryBean()
}
