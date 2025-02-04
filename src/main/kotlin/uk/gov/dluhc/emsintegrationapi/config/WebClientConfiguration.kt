package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.dluhc.logging.rest.CorrelationIdWebClientMdcExchangeFilter

@Configuration
class WebClientConfiguration {

    private var correlationIdExchangeFilter: CorrelationIdWebClientMdcExchangeFilter = CorrelationIdWebClientMdcExchangeFilter()

    @Bean
    fun eroManagementWebClient(
        @Value("\${api.ero-management.url}") eroManagementApiUrl: String
    ): WebClient =
        WebClient.builder()
            .baseUrl(eroManagementApiUrl)
            .filter(correlationIdExchangeFilter)
            .build()
}
