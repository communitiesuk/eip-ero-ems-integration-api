package uk.gov.dluhc.emsintegrationapi.cucumber.healthcheck

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

data class HealthStatus(val status: String)
class HealthCheckSteps(private val webTestClient: WebTestClient) : En {

    var apiHeaderSpec: WebTestClient.RequestHeadersSpec<*>? = null
    var apiResponseSpec: WebTestClient.ResponseSpec? = null

    init {
        Given("The service is up and running") {
            apiHeaderSpec = webTestClient.get().uri("/actuator/health")
        }
        When("I send a health check request") {
            apiResponseSpec = apiHeaderSpec!!.exchange()
        }
        Then("I will get the status is {string}") { status: String ->

            // Then
            apiResponseSpec!!.expectStatus().isOk
            val actual = apiResponseSpec!!.returnResult<HealthStatus>().responseBody.blockFirst()
            assertThat(actual!!.status).isEqualTo(status)
        }
    }
}
