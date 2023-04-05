package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat

class ApiResponseValidationSteps(val apiResponse: ApiResponse) : En {
    init {
        Then("I received an error with the response status as {int}") { httpStatus: Int ->
            assertThat(apiResponse.responseSpec).isNotNull
            apiResponse.responseSpec!!.expectStatus().isEqualTo(httpStatus)
        }
        And("it has an error message of {string}") { errorMessage: String ->
            val message = apiResponse.responseSpec!!.returnResult(String::class.java).responseBody.blockFirst()
            assertThat(message).isEqualTo(errorMessage)
        }
    }
}
