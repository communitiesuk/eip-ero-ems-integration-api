package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat

class ApiResponseValidationSteps(val apiResponse: ApiResponse) : En {
    init {
        Then("I receive error with response status as {int}") { httpStatus: Int ->
            assertThat(apiResponse.responseSpec).isNotNull
            apiResponse.responseSpec!!.expectStatus().isEqualTo(httpStatus)
        }
    }
}
