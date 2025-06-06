package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessProxyVoteApplicationMessageService

private val logger = KotlinLogging.logger { }

@Component
class ProxyVoteApplicationMessageListener(private val proxyVoteApplicationMessageService: ProcessProxyVoteApplicationMessageService) {
    @SqsListener("\${sqs.proxy-application-queue-name}")
    fun handleMessage(@Valid @Payload proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {
        with(proxyVoteApplicationMessage) {
            logger.info("Proxy Vote Application Message received with an application id = ${applicationDetails.id}")
            proxyVoteApplicationMessageService.process(this)
        }
    }
}
