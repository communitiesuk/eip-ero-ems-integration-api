package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.Companion.SQS_CONFIGURATION_PREFIX
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.EMS_APPLICATION_PROCESSED_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE

@EnableConfigurationProperties
@ConfigurationProperties(prefix = SQS_CONFIGURATION_PREFIX)
@ConstructorBinding
class QueueConfiguration(
    private val proxyApplicationQueueName: String,
    private val postalApplicationQueueName: String,
    private val deletedProxyApplicationQueueName: String,
    private val deletedPostalApplicationQueueName: String,
    private val emsApplicationProcessedQueueName: String,
) {
    companion object {
        const val SQS_CONFIGURATION_PREFIX = "sqs"
    }

    fun getQueueNameFrom(queueName: QueueName): String {
        return when (queueName) {
            POSTAL_APPLICATION_QUEUE -> postalApplicationQueueName
            PROXY_APPLICATION_QUEUE -> proxyApplicationQueueName
            DELETED_POSTAL_APPLICATION_QUEUE -> deletedPostalApplicationQueueName
            DELETED_PROXY_APPLICATION_QUEUE -> deletedProxyApplicationQueueName
            EMS_APPLICATION_PROCESSED_QUEUE -> emsApplicationProcessedQueueName
        }
    }

    enum class QueueName {
        POSTAL_APPLICATION_QUEUE,
        PROXY_APPLICATION_QUEUE,
        DELETED_POSTAL_APPLICATION_QUEUE,
        DELETED_PROXY_APPLICATION_QUEUE,
        EMS_APPLICATION_PROCESSED_QUEUE
    }
}
