package uk.gov.dluhc.emsintegrationapi.rest

import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.config.database.TransactionRoutingDataSource
import uk.gov.dluhc.emsintegrationapi.rest.GetPendingRegisterChecksIntegrationTest.Companion.CERT_SERIAL_NUMBER_VALUE
import uk.gov.dluhc.emsintegrationapi.rest.GetPendingRegisterChecksIntegrationTest.Companion.REQUEST_HEADER_NAME
import uk.gov.dluhc.emsintegrationapi.testsupport.getRandomGssCode
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildRegisterCheckResultRequest
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import uk.gov.dluhc.registercheckerapi.models.PendingRegisterChecksResponse
import uk.gov.dluhc.registercheckerapi.models.RegisterCheckResultRequest
import java.util.UUID

internal class TransactionRoutingDataSourceIntegrationTest : IntegrationTest() {
    @Test
    fun `should use read-only data source for get function`() {
        // Given
        val eroId = "camden-city-council"
        val gssCode = getRandomGssCode()

        wireMockService.stubIerApiGetEros(CERT_SERIAL_NUMBER_VALUE, eroId, listOf(gssCode))

        // When
        webTestClient
            .get()
            .uri("/registerchecks")
            .header(
                REQUEST_HEADER_NAME,
                CERT_SERIAL_NUMBER_VALUE,
            ).exchange()
            .expectStatus()
            .isOk
            .returnResult(PendingRegisterChecksResponse::class.java)

        // Then
        then(readOnlyDataSource).should(atLeastOnce()).connection
    }

    @Test
    fun `should use read-write data source for updating function`() {
        // Given
        val requestId = UUID.fromString("322ff65f-a0a1-497d-a224-04800711a1fb")
        val eroId = "camden-city-council"
        val firstGssCode = "E12345678"
        val secondGssCode = "E98764532"
        val gssCodes = listOf(firstGssCode, secondGssCode)

        wireMockService.stubIerApiGetEros(CERT_SERIAL_NUMBER_VALUE, eroId, gssCodes)

        // When
        webTestClient
            .post()
            .uri("/registerchecks/$requestId")
            .header(REQUEST_HEADER_NAME, CERT_SERIAL_NUMBER_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(buildRegisterCheckResultRequest(requestId = requestId)),
                RegisterCheckResultRequest::class.java,
            ).exchange()
            .expectStatus()
            .isNotFound
            .returnResult(ErrorResponse::class.java)

        // Then
        then(readOnlyDataSource).should(never()).connection
        then(readWriteDataSource).should(atLeastOnce()).connection
    }

    @Test
    fun `should have exactly three data sources`() {
        // The TransactionRoutingDataSource and the reading/writing pools.
        // This is important, because a slight change to config can cause AutoConfiguration
        // to pollute the context with stray DataSources.
        assertThat(dataSources).hasSize(3)
        assertThat(dataSources).hasExactlyElementsOfTypes(
            TransactionRoutingDataSource::class.java,
            HikariDataSource::class.java,
            HikariDataSource::class.java,
        )
    }
}
