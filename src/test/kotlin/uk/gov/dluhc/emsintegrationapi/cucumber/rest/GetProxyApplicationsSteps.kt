package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient.Companion.buildUriStringWithQueryParam
import uk.gov.dluhc.emsintegrationapi.config.ApiClient.Companion.validateStatusAndGetResponse
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.saveRecords
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ProxyVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication

private val logger = KotlinLogging.logger { }

class GetProxyApplicationsSteps(
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    webClient: WebTestClient,
    apiProperties: ApiProperties,
    val apiResponse: ApiResponse
) : En {
    private var proxyVoteApplicationsMap: Map<String, ProxyVoteApplication>? = null
    private var proxyVoteAcceptedResponse: ProxyVoteAcceptedResponse? = null
    private val apiClient = ApiClient(webClient, apiProperties)

    companion object {
        const val ACCEPTED_PATH = "/proxyVotes/accepted"
    }

    init {
        Given("there are {int} proxy vote applications exist with the status {string}") { numberOfRecords: Int, recordStatus: String ->
            logger.info(
                "Creating $numberOfRecords of proxy vote applications"
            )
            val proxyVoteApplications = saveRecords(
                proxyVoteApplicationRepository, numberOfRecords
            ) { buildProxyVoteApplication(recordStatus = RecordStatus.valueOf(recordStatus)) }
            // Let us create a map out of it so it will be easy for the validation
            proxyVoteApplicationsMap = proxyVoteApplications.associateBy { it.applicationId }
        }
        When("I send a get proxy vote applications request with the page size {int}") { pageSize: Int ->
            logger.info { "Sending get request with page size $pageSize" }
            apiResponse.responseSpec = apiClient.get(buildUriStringWithQueryParam(ACCEPTED_PATH, pageSize))
        }
        Then("I received a response with {int} proxy vote applications") { expectedPageSize: Int ->
            logger.info("Expected number of proxy vote applications = $expectedPageSize")
            proxyVoteAcceptedResponse = validateStatusAndGetResponse(
                apiResponse.responseSpec!!,
                expectedHttpStatus = 200,
                ProxyVoteAcceptedResponse::class.java
            )
            assertThat(proxyVoteAcceptedResponse).isNotNull
            assertThat(proxyVoteAcceptedResponse!!.proxyVotes).hasSize(expectedPageSize)
            validateTheResponse()
        }
        When("I send a get proxy vote request without the page size") {
            logger.info { "Sending get request without page size" }
            apiResponse.responseSpec = apiClient.get(ACCEPTED_PATH)
        }
        When("I send a get proxy vote applications request without a certificate serial number in the request header") {
            apiResponse.responseSpec = apiClient.get(ACCEPTED_PATH, attachSerialNumber = false)
        }
    }

    private fun validateTheResponse() {
        proxyVoteAcceptedResponse!!.proxyVotes!!.forEach { proxyVote ->
            ProxyVoteAssert.assertThat(proxyVote)
                .hasCorrectFieldsFromProxyApplication(proxyVoteApplicationsMap!![proxyVote.id]!!)
        }
    }
}
