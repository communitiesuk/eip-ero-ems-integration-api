package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration(
    private val correlationIdExchangeFilter: CorrelationIdWebClientMdcExchangeFilter
) {

    @Bean
    fun eroManagementWebClient(@Value("\${api.ero-management.url}") eroManagementApiUrl: String): WebClient =
        WebClient.builder()
            .baseUrl(eroManagementApiUrl)
            .filter(correlationIdExchangeFilter)
            .build()
}
