package uk.gov.dluhc.emsintegrationapi.rest.healthcheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest

data class HealthStatus(
    val status: String,
)

class HealthCheckIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    var apiHeaderSpec: WebTestClient.RequestHeadersSpec<*>? = null
    var apiResponseSpec: WebTestClient.ResponseSpec? = null

    @Test
    fun `The service is up and running`() {
        // Given
        apiHeaderSpec = webTestClient.get().uri("/actuator/health")

        // When
        apiResponseSpec = apiHeaderSpec!!.exchange()

        // Then
        apiResponseSpec!!.expectStatus().isOk
        val actual = apiResponseSpec!!.returnResult<HealthStatus>().responseBody.blockFirst()
        assertThat(actual!!.status).isEqualTo("UP")
    }
}
