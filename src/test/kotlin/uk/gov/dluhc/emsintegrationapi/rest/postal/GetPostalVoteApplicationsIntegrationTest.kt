package uk.gov.dluhc.emsintegrationapi.rest.postal

import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.PostalIntegrationTestHelpers

class GetPostalVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    private lateinit var queueMessagingTemplate: QueueMessagingTemplate

    private var apiClient: ApiClient? = null

    private var testHelpers: PostalIntegrationTestHelpers? = null

    private val acceptedPath = "/postalvotes"

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(postalRepository = postalVoteApplicationRepository)
        apiClient = ApiClient(webClient, apiProperties)
        testHelpers =
            PostalIntegrationTestHelpers(
                wiremockService = wireMockService,
                postalVoteApplicationRepository = postalVoteApplicationRepository,
                queueMessagingTemplate = queueMessagingTemplate,
            )
        testHelpers!!.givenEroIdAndGssCodesMapped()
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When I send a get postal vote applications request without a certificate serial number in the request header
        val responseSpec = apiClient!!.get(acceptedPath, attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the page size is greater than the configured page size 50`() {
        // When I send a get postal vote applications request with the page size 51 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                51,
            )

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "The page size must be greater than or equal to 1 and less than or equal to 50"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The page size must be greater than or equal to 1 and less than or equal to 50")
    }

    @Test
    fun `System does not have any postal vote applications`() {
        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                10,
            )

        // Then I received a response with 0 postal vote applications with signature
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = mapOf(),
            hasSignature = true,
            expectedPageSize = 0,
            responseSpec,
        )
    }

    @Test
    fun `System returns http status 404 if the attached certificate serial number does not exist`() {
        // Given the certificate serial "INVALID123" does not exist in ERO
        wireMockService.stubIerApiGetEroIdentifierThrowsNotFoundError("INVALID123")

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "INVALID123"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                "/postalvotes",
                "INVALID123",
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The EROCertificateMapping for certificateSerial=[INVALID123] could not be found"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The EROCertificateMapping for certificateSerial=[INVALID123] could not be found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get mapping request`() {
        // Given the ERO could not process the get mapping request for "1234567899"
        wireMockService.stubIerApiGetEroIdentifierThrowsInternalServerError("1234567899")

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567899"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567899",
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And it has an error message of "Unable to retrieve EROCertificateMapping for certificate serial [1234567899] due to error: [500 Server Error: \"Error\"]"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(
            message,
        ).isEqualTo(
            "Unable to retrieve EROCertificateMapping for certificate serial [1234567899] due to error: [500 Server Error: \"Error\"]",
        )
    }

    @Test
    fun `System returns http status 404 if ERO Mapping Id does not exist`() {
        // Given the ERO Id "camden-city-council" does not exist in ERO
        wireMockService.stubEroManagementGetEroThrowsNotFoundError("camden-city-council")

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The ERO camden-city-council could not be found"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The ERO camden-city-council could not be found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get gss code request`() {
        // Given the ERO could not process the get gss codes request for "camden-city-council"
        wireMockService.stubEroManagementGetEroThrowsInternalServerError("camden-city-council")

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And the error message contains "Unable to retrieve GSS Codes for camden-city-council due to error: [500 Internal Server Error from GET"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(
            message,
        ).startsWith("Unable to retrieve GSS Codes for camden-city-council due to error: [500 Internal Server Error from GET")
    }

    @Test
    fun `System returns postal vote applications of a given page size`() {
        // Given there are 20 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(20, "RECEIVED", "E12345678", "E12345679")

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                10,
            )

        // Then I received a response with 10 postal vote applications with signature
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 10,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System does not have requested number of postal applications`() {
        // Given there are 2 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(2, "RECEIVED", "E12345678", "E12345679")

        // When I send a get postal vote applications request with the page size 3 and the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                "1234567891",
                3,
            )

        // Then I received a response with 2 postal vote applications with signature
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 2,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System returns default number of records if page size is not specified`() {
        // Given there are 21 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(21, "RECEIVED", "E12345678", "E12345679")

        // When I send a get postal vote request without the page size and with the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, "1234567891")

        // Then I received a response with 20 postal vote applications with signature
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 20,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System returns postal vote applications with signature waiver reason`() {
        // Given there are 21 postal vote applications without signature exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val postalVoteApplicationMap =
            testHelpers!!.buildPostalVoteApplications(
                numberOfRecords = 21,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf("E12345678", "E12345679"),
                signatureBase64 = null,
                signatureWaived = true,
                signatureWaiverReason = SIGNATURE_WAIVER_REASON,
            )

        // When I send a get postal vote request without the page size and with the certificate serial number "1234567891"
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, "1234567891")

        // Then I received a response with 20 postal vote applications with signature waiver
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = false,
            expectedPageSize = 20,
            apiResponse = responseSpec,
        )
    }
}
