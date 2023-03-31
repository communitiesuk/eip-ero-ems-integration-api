package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient.Companion.buildUriStringWithQueryParam
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.saveRecords
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.PostalVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication

private val logger = KotlinLogging.logger { }

class GetPostalApplicationsSteps(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    webClient: WebTestClient,
    apiProperties: ApiProperties
) : En {
    private var postalVoteApplicationsMap: Map<String, PostalVoteApplication>? = null
    private var postalVoteAcceptedResponse: PostalVoteAcceptedResponse? = null
    private val apiClient = ApiClient(webClient, apiProperties)
    private var responseSpec: WebTestClient.ResponseSpec? = null

    companion object {
        const val ROOT_PATH = "/postalVotes"
        const val ACCEPTED = "/accepted"
    }

    init {
        Given("there are {int} postal vote applications exist with the status {string}") { numberOfRecords: Int, recordStatus: String ->
            logger.info(
                "Creating $numberOfRecords of postal vote applications"
            )
            val postalVoteApplications = saveRecords(
                postalVoteApplicationRepository, numberOfRecords
            ) { buildPostalVoteApplication(recordStatus = RecordStatus.valueOf(recordStatus)) }
            // Let us create a map out of it so it will easy for validation
            postalVoteApplicationsMap = postalVoteApplications.associateBy { it.applicationId }
        }
        When("I send a get postal vote applications request with the page size {int}") { pageSize: Int ->
            logger.info { "Sending get request with page size $pageSize" }
            postalVoteAcceptedResponse = apiClient.get(
                buildUriStringWithQueryParam(getPath(), pageSize),
                PostalVoteAcceptedResponse::class.java,
                true
            )
        }
        Then("I received a response with {int} postal vote applications") { expectedPageSize: Int ->

            logger.info("Expected number of postal vote applications = $expectedPageSize")
            assertThat(postalVoteAcceptedResponse).isNotNull
            assertThat(postalVoteAcceptedResponse!!.proxyVotes).hasSize(expectedPageSize)
            validateTheResponse()
        }
        When("I send a get postal vote request without the page size") {
            logger.info { "Sending get request without page size" }
            postalVoteAcceptedResponse = apiClient.get(
                getPath(),
                PostalVoteAcceptedResponse::class.java,
                true
            )
        }
        When("I send a get postal vote applications request without a certificate serial number in the request header") {
            responseSpec = apiClient.get(getPath(), attachSerialNumber = false)
        }
        Then("I receive error with response status as {int}") { httpStatus: Int ->
            assertThat(responseSpec).isNotNull
            responseSpec!!.expectStatus().isEqualTo(httpStatus)
        }
    }

    private fun getPath() = "$ROOT_PATH/$ACCEPTED"
    private fun validateTheResponse() {
        postalVoteAcceptedResponse!!.proxyVotes!!.forEach { postalVote ->
            PostalVoteAssert.assertThat(postalVote)
                .hasCorrectFieldsFromPostalApplication(postalVoteApplicationsMap!![postalVote.id]!!)
        }
    }
}
