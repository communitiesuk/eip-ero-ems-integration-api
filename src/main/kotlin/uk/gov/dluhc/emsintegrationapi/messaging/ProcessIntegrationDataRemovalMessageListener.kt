package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessIntegrationDataRemovalMessageService

private val logger = KotlinLogging.logger { }

@Component
class ProcessIntegrationDataRemovalMessageListener(private val removeEmsIntegrationDataMessageService: ProcessIntegrationDataRemovalMessageService) {

    @SqsListener("\${sqs.remove-application-ems-integration-data-queue-name}")
    fun handleMessage(@Valid @Payload removeEmsDataMessage: RemoveApplicationEmsIntegrationDataMessage) {
        with(removeEmsDataMessage) {
            logger.info("Integration Data Removal Message received with an application id = ${removeEmsDataMessage.applicationId} and source ${removeEmsDataMessage.source}")
            removeEmsIntegrationDataMessageService.process(this)
        }
    }
}
