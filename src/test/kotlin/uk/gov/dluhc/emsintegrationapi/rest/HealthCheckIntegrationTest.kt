package uk.gov.dluhc.emsintegrationapi.rest

import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest

internal class HealthCheckIntegrationTest : IntegrationTest() {
    @Test
    fun `should return health check status UP given microservice is running healthily`() {
        // Given
        val request = webTestClient.get().uri("/actuator/health")

        // When
        val response = request.exchange()

        // Then
        response.expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }
}
