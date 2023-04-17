package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties

class DeleteApplicationSteps(
    webClient: WebTestClient,
    apiProperties: ApiProperties,
    private val apiResponse: ApiResponse
) : En {
    private val apiClient = ApiClient(webClient, apiProperties)

    init {
        When("the EMS send a delete request to {string} with an application id {string}") { deletePath: String, applicationId: String ->
            apiResponse.responseSpec = apiClient.delete("$deletePath/$applicationId")
        }
        When("the EMS send a delete request to {string} with an application id {string} and the certificate serial number {string}") { deletePath: String, applicationId: String, certificateSerialNumber: String ->
            apiResponse.responseSpec =
                apiClient.delete("$deletePath/$applicationId", serialNumber = certificateSerialNumber)
        }
        When("the EMS send a delete request to {string} with an application id {string} and without the certificate serial number in the request header") { deletePath: String, applicationId: String ->
            apiResponse.responseSpec = apiClient.delete("$deletePath/$applicationId", attachSerialNumber = false)
        }
    }
}
