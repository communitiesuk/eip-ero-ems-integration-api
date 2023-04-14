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
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.PostalVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovalDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication

private val logger = KotlinLogging.logger { }

class GetPostalApplicationsSteps(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    webClient: WebTestClient,
    apiProperties: ApiProperties,
    val apiResponse: ApiResponse
) : En {
    private var postalVoteApplicationsMap: Map<String, PostalVoteApplication>? = null
    private var postalVoteAcceptedResponse: PostalVoteAcceptedResponse? = null
    private val apiClient = ApiClient(webClient, apiProperties)

    companion object {
        const val ACCEPTED_PATH = "/postalVotes/accepted"
    }

    init {
        Given("there are {int} postal vote applications exist with the status {string} and GSS Codes {string},{string}") { numberOfRecords: Int, recordStatus: String, gssCode1: String, gssCode2: String ->
            logger.info("Creating $numberOfRecords of postal vote applications")
            val postalVoteApplications = saveRecords(
                postalVoteApplicationRepository, numberOfRecords
            ) {
                buildPostalVoteApplication(
                    recordStatus = RecordStatus.valueOf(recordStatus),
                    approvalDetails = buildApprovalDetailsEntity(gssCode = faker.options().option(gssCode1, gssCode2))
                )
            }
            // Let us create a map out of it so it will easy for validation
            postalVoteApplicationsMap = postalVoteApplications.associateBy { it.applicationId }
        }
        When("I send a get postal vote applications request with the page size {int} and the certificate serial number {string}") { pageSize: Int, certificateSerialNumber: String ->
            logger.info { "Sending get request with page size $pageSize" }
            apiResponse.responseSpec = apiClient.get(
                buildUriStringWithQueryParam(ACCEPTED_PATH, pageSize),
                serialNumber = certificateSerialNumber
            )
        }
        Then("I received a response with {int} postal vote applications") { expectedPageSize: Int ->
            logger.info("Expected number of postal vote applications = $expectedPageSize")
            postalVoteAcceptedResponse =
                validateStatusAndGetResponse(
                    apiResponse.responseSpec!!,
                    expectedHttpStatus = 200,
                    PostalVoteAcceptedResponse::class.java
                )
            assertThat(postalVoteAcceptedResponse).isNotNull
            assertThat(postalVoteAcceptedResponse!!.proxyVotes).hasSize(expectedPageSize)
            validateTheResponse()
        }
        When("I send a get postal vote request without the page size and with the certificate serial number {string}") { certificateSerialNumber: String ->
            logger.info { "Sending get request without page size" }
            apiResponse.responseSpec = apiClient.get(ACCEPTED_PATH, serialNumber = certificateSerialNumber)
        }
        When("I send a get postal vote applications request without a certificate serial number in the request header") {
            apiResponse.responseSpec = apiClient.get(ACCEPTED_PATH, attachSerialNumber = false)
        }
    }

    private fun validateTheResponse() {
        postalVoteAcceptedResponse!!.proxyVotes!!.forEach { postalVote ->
            PostalVoteAssert.assertThat(postalVote)
                .hasCorrectFieldsFromPostalApplication(postalVoteApplicationsMap!![postalVote.id]!!)
        }
    }
}
