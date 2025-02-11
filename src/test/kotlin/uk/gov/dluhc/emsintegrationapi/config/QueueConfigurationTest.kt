package uk.gov.dluhc.emsintegrationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_PROXY_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE

internal class QueueConfigurationTest {

    private val queueConfiguration =
        QueueConfiguration(
            PROXY_APPLICATION_QUEUE.name,
            POSTAL_APPLICATION_QUEUE.name,
            DELETED_PROXY_APPLICATION_QUEUE.name,
            DELETED_POSTAL_APPLICATION_QUEUE.name
        )

    @Test
    fun `should return queue name from enum`() {
        QueueName.values()
            .forEach { queueName -> assertThat(queueConfiguration.getQueueNameFrom(queueName)).isEqualTo(queueName.name) }
    }
}
