package uk.gov.dluhc.emsintegrationapi.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.service.ProcessProxyVoteApplicationMessageService
import uk.gov.dluhc.messagingsupport.MessageListener

private val logger = KotlinLogging.logger { }

@Component
class ProxyVoteApplicationMessageListener(
    private val proxyVoteApplicationMessageService: ProcessProxyVoteApplicationMessageService
) : MessageListener<ProxyVoteApplicationMessage> {
    @SqsListener("\${sqs.proxy-application-queue-name}")
    override fun handleMessage(@Valid @Payload payload: ProxyVoteApplicationMessage) {
        with(payload) {
            logger.info { "Proxy Vote Application Message received with an application id = ${applicationDetails.id}" }
            proxyVoteApplicationMessageService.process(this)
        }
    }
}
