package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus

class PutApplicationSteps(
    webClient: WebTestClient,
    apiProperties: ApiProperties,
    private val apiResponse: ApiResponse
) : En {
    private val apiClient = ApiClient(webClient, apiProperties)

    init {
        When("the EMS sends a put request to {string} with an application id {string}") { putPath: String, applicationId: String ->
            apiResponse.responseSpec = apiClient.putEmsApplication(uri = "$putPath/$applicationId")
        }
        When("the EMS sends a put request to {string} with an application id {string} and certificate serial number {string} and SUCCESS status") { putPath: String, applicationId: String, certificateSerialNumber: String ->
            apiResponse.responseSpec =
                apiClient.postEmsApplication(uri = "$putPath/$applicationId", serialNumber = certificateSerialNumber)
        }
        When("the EMS sends a put request to {string} with an application id {string} and certificate serial number {string} and FAILURE status") { putPath: String, applicationId: String, certificateSerialNumber: String ->
            val request = EMSApplicationResponse(status = EMSApplicationStatus.FAILURE, message = ApplicationConstants.EMS_MESSAGE_TEXT, details = ApplicationConstants.EMS_DETAILS_TEXT)
            apiResponse.responseSpec =
                apiClient.postEmsApplication(uri = "$putPath/$applicationId", serialNumber = certificateSerialNumber, request = request)
        }
        When("the EMS sends a put request to {string} with an application id {string} and without the certificate serial number in the request header") { putPath: String, applicationId: String ->
            apiResponse.responseSpec = apiClient.putEmsApplication(uri = "$putPath/$applicationId", attachSerialNumber = false)
        }
    }
}
