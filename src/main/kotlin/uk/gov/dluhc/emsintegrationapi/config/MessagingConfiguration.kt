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
import uk.gov.dluhc.registercheckerapi.messaging.models.PendingRegisterCheckArchiveMessage
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResultMessage

@Configuration
class MessagingConfiguration {

    @Value("\${sqs.confirm-applicant-register-check-result-queue-name}")
    private lateinit var pendingRegisterCheckResultQueueName: String

    @Value("\${sqs.postal-vote-confirm-applicant-register-check-result-queue-name}")
    private lateinit var postalVoteConfirmRegisterCheckResultQueueName: String

    @Value("\${sqs.proxy-vote-confirm-applicant-register-check-result-queue-name}")
    private lateinit var proxyVoteConfirmRegisterCheckResultQueueName: String

    @Value("\${sqs.overseas-vote-confirm-applicant-register-check-result-queue-name}")
    private lateinit var overseasVoteConfirmRegisterCheckResultQueueName: String

    @Value("\${sqs.register-check-result-response-queue-name}")
    private lateinit var registerCheckResultResponseQueueName: String

    @Value("\${sqs.pending-register-check-archive-queue-name}")
    private lateinit var pendingRegisterCheckArchiveQueueName: String

    @Bean(name = ["confirmRegisterCheckResultQueue"])
    fun confirmRegisterCheckResultQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(pendingRegisterCheckResultQueueName, sqsTemplate)

    @Bean(name = ["postalVoteConfirmRegisterCheckResultQueue"])
    fun postalVoteConfirmRegisterCheckResultQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(postalVoteConfirmRegisterCheckResultQueueName, sqsTemplate)

    @Bean(name = ["proxyVoteConfirmRegisterCheckResultQueue"])
    fun proxyVoteConfirmRegisterCheckResultQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(proxyVoteConfirmRegisterCheckResultQueueName, sqsTemplate)

    @Bean(name = ["overseasVoteConfirmRegisterCheckResultQueue"])
    fun overseasVoteConfirmRegisterCheckResultQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(overseasVoteConfirmRegisterCheckResultQueueName, sqsTemplate)

    @Bean(name = ["registerCheckResultResponseQueue"])
    fun registerCheckResultResponseQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<RegisterCheckResultMessage>(registerCheckResultResponseQueueName, sqsTemplate)

    @Bean(name = ["pendingRegisterCheckArchiveQueue"])
    fun pendingRegisterCheckArchiveQueue(sqsTemplate: SqsTemplate) =
        MessageQueue<PendingRegisterCheckArchiveMessage>(pendingRegisterCheckResultQueueName, sqsTemplate)

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
