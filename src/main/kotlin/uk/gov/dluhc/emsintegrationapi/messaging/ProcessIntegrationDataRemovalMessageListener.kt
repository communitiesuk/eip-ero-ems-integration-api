package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessIntegrationDataRemovalMessageService
import uk.gov.dluhc.messagingsupport.MessageListener

private val logger = KotlinLogging.logger { }

@Component
class ProcessIntegrationDataRemovalMessageListener(
    private val removeEmsIntegrationDataMessageService: ProcessIntegrationDataRemovalMessageService
) : MessageListener<RemoveApplicationEmsIntegrationDataMessage> {

    @SqsListener("\${sqs.remove-application-ems-integration-data-queue-name}")
    override fun handleMessage(@Valid @Payload payload: RemoveApplicationEmsIntegrationDataMessage) {
        with(payload) {
            logger.info { "Integration Data Removal Message received with an application id = $applicationId and source $source" }
            removeEmsIntegrationDataMessageService.process(this)
        }
    }
}
