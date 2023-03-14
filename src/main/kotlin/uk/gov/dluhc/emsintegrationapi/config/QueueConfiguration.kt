package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.Companion.SQS_CONFIGURATION_PREFIX
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE

@EnableConfigurationProperties
@ConfigurationProperties(prefix = SQS_CONFIGURATION_PREFIX)
@ConstructorBinding
class QueueConfiguration(
    val proxyApplicationQueueName: String,
    val postalApplicationQueueName: String,
    val deletedProxyApplicationQueueName: String,
    val deletedPostalApplicationQueueName: String,
) {
    companion object {
        const val SQS_CONFIGURATION_PREFIX = "sqs"
    }

    fun getQueueNameFrom(queueName: QueueName): String {
        return when (queueName) {
            POSTAL_APPLICATION_QUEUE -> postalApplicationQueueName
            PROXY_APPLICATION_QUEUE -> postalApplicationQueueName
            DELETED_POSTAL_APPLICATION_QUEUE -> postalApplicationQueueName
            DELETED_PROXY_APPLICATION_QUEUE -> postalApplicationQueueName
        }
    }

    enum class QueueName {
        POSTAL_APPLICATION_QUEUE,
        PROXY_APPLICATION_QUEUE,
        DELETED_POSTAL_APPLICATION_QUEUE,
        DELETED_PROXY_APPLICATION_QUEUE,
    }
}
