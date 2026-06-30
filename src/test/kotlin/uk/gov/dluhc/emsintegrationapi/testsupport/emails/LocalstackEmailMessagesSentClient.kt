package uk.gov.dluhc.emsintegrationapi.testsupport.emails

import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import java.net.URI

private val logger = KotlinLogging.logger {}

/**
 * This class allows access to Localstack's list of sent emails.
 */
@Component
class LocalstackEmailMessagesSentClient(
    private val jsonMapper: JsonMapper
) {

    fun getEmailMessagesSent(url: String): LocalstackEmailMessages {
        val webClient = WebClient.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(jsonMapper, MediaType.APPLICATION_JSON))
                configurer.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(jsonMapper, MediaType.APPLICATION_JSON))
            }
            .build()

        val response = webClient.get().uri(URI.create(url))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(LocalstackEmailMessages::class.java)
            .onErrorResume { ex -> handleException(ex, "Error getting email messages from Localstack") }
            .block()

        return response!!
    }

    private fun handleException(ex: Throwable, message: String): Mono<LocalstackEmailMessages> {
        logger.error(ex) { "Unhandled exception thrown by WebClient" }
        return Mono.error(RuntimeException(message))
    }
}
