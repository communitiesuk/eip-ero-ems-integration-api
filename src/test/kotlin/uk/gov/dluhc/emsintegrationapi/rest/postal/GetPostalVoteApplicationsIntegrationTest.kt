package uk.gov.dluhc.emsintegrationapi.rest.postal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.config.TestClockConfiguration
import uk.gov.dluhc.emsintegrationapi.database.repository.EroAbsentVoteHoldRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_99
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_INVALID
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1_CERTIFICATE_SERIAL
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1_GSS_CODE_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1_GSS_CODE_2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.PostalIntegrationTestHelpers
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import java.time.Clock
import java.time.ZoneOffset

internal class GetPostalVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    protected lateinit var clock: TestClockConfiguration.FlexibleClock

    @Autowired
    private lateinit var eroAbsentVoteHoldRepository: EroAbsentVoteHoldRepository

    private var apiClient: ApiClient? = null

    private var testHelpers: PostalIntegrationTestHelpers? = null

    private val acceptedPath = "/postalvotes"

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        eroAbsentVoteHoldRepository.deleteAll()
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            registerCheckResultDataRepository = registerCheckResultDataRepository
        )
        apiClient = ApiClient(webClient, apiProperties)
        testHelpers =
            PostalIntegrationTestHelpers(
                wiremockService = wireMockService,
                postalVoteApplicationRepository = postalVoteApplicationRepository,
                queueMessagingTemplate = sqsMessagingTemplate,
                eroAbsentVoteHoldRepository = eroAbsentVoteHoldRepository,
            )
        // Map ERO_ID_1_CERTIFICATE_SERIAL to ERO_ID_1 with gss codes ERO_ID_1_GSS_CODE_1 and ERO_ID_1_GSS_CODE_2
        testHelpers!!.givenEroIdAndGssCodesMapped()
        val timeBeforeThreshold = apiProperties.holdingPoolThresholdDate.minusSeconds(3600)
        clock.setClock(Clock.fixed(timeBeforeThreshold, ZoneOffset.UTC))
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
        // When I send a get postal vote applications request with the page size 51 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
                51,
            )

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "The page size must be greater than or equal to 1 and less than or equal to 50"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            message!!.message,
        ).isEqualTo("getPostalVoteApplications.pageSize: The page size must be greater than or equal to 1 and less than or equal to 50")
    }

    @Test
    fun `System does not have any postal vote applications`() {
        // When I send a get postal vote applications request with the page size 10 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
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
        // Given the certificate serial INVALID_SERIAL_NUM does not exist in ERO
        wireMockService.stubIerApiGetEroIdentifierThrowsNotFoundError(CERTIFICATE_SERIAL_NUM_INVALID)

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_INVALID
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                "/postalvotes",
                CERTIFICATE_SERIAL_NUM_INVALID,
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "EROCertificateMapping for certificateSerial=[INVALID_SERIAL_NUM] not found"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(message!!.message).isEqualTo("EROCertificateMapping for certificateSerial=[$CERTIFICATE_SERIAL_NUM_INVALID] not found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get mapping request`() {
        // Given the ERO could not process the get mapping request for CERTIFICATE_SERIAL_NUM_99
        wireMockService.stubIerInternalServerError()

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_99
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_99,
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And it has an error message of "Unable to retrieve EROCertificateMapping for certificate serial [CERTIFICATE_SERIAL_NUM_99] due to error: [500 Server Error: \"Error\"]"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(message!!.message).isEqualTo("Error retrieving EROs from IER API")
    }

    @Test
    fun `System returns http status 404 if ERO Mapping Id does not exist`() {
        // Given the ERO Id "camden-city-council" does not exist in ERO
        wireMockService.stubIerApiGetNoEros()

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "EROCertificateMapping for certificateSerial=[1234567891] not found"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(message!!.message).isEqualTo("EROCertificateMapping for certificateSerial=[1234567891] not found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get gss code request`() {
        // Given the ERO could not process the get gss codes request for "camden-city-council"
        wireMockService.stubIerInternalServerError()

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And it has an error message of "Error retrieving EROs from IER API"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            message!!.message,
        ).isEqualTo("Error retrieving EROs from IER API")
    }

    @Test
    fun `System returns postal vote applications of a given page size`() {
        // Given there are 20 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(20, "RECEIVED", ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2)

        // When I send a get postal vote applications request with the page size 10 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
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
        // Given there are 2 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(2, "RECEIVED", ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2)

        // When I send a get postal vote applications request with the page size 3 and the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                ERO_ID_1_CERTIFICATE_SERIAL,
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
        // Given there are 21 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2
        val postalVoteApplicationMap = testHelpers!!.buildPostalVoteApplications(21, "RECEIVED", ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2)

        // When I send a get postal vote request without the page size and with the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, ERO_ID_1_CERTIFICATE_SERIAL)

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
        // Given there are 21 postal vote applications without signature exist with the status "RECEIVED" and GSS Codes ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2
        val postalVoteApplicationMap =
            testHelpers!!.buildPostalVoteApplications(
                numberOfRecords = 21,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf(ERO_ID_1_GSS_CODE_1, ERO_ID_1_GSS_CODE_2),
                signatureBase64 = null,
                signatureWaived = true,
                signatureWaiverReason = SIGNATURE_WAIVER_REASON,
            )

        // When I send a get postal vote request without the page size and with the certificate serial number ERO_ID_1_CERTIFICATE_SERIAL
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, ERO_ID_1_CERTIFICATE_SERIAL)

        // Then I received a response with 20 postal vote applications with signature waiver
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = false,
            expectedPageSize = 20,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System does not return postal vote applications if hold is enabled and threshold date has passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.plusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        testHelpers!!.createEroAbsentVoteHold(eroId = ERO_ID_1, holdEnabled = true)

        testHelpers!!.buildPostalVoteApplications(
            numberOfRecords = 10,
            recordStatus = "RECEIVED",
            gssCodes = arrayOf(ERO_ID_1_GSS_CODE_1),
        )

        // When
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, ERO_ID_1_CERTIFICATE_SERIAL)

        // Then
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = mapOf(),
            hasSignature = true,
            expectedPageSize = 0,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System returns postal vote applications if hold not enabled and threshold date has passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.plusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        testHelpers!!.createEroAbsentVoteHold(eroId = ERO_ID_1, holdEnabled = false)

        val postalVoteApplicationMap =
            testHelpers!!.buildPostalVoteApplications(
                numberOfRecords = 1,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf(ERO_ID_1_GSS_CODE_1),
            )

        // When
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, ERO_ID_1_CERTIFICATE_SERIAL)

        // Then
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 1,
            apiResponse = responseSpec,
        )
    }

    @Test
    fun `System returns postal vote applications if hold enabled and threshold date has not passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.minusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        testHelpers!!.createEroAbsentVoteHold(eroId = ERO_ID_1, holdEnabled = true)

        val postalVoteApplicationMap =
            testHelpers!!.buildPostalVoteApplications(
                numberOfRecords = 1,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf(ERO_ID_1_GSS_CODE_1),
            )

        // When
        val responseSpec =
            testHelpers!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, ERO_ID_1_CERTIFICATE_SERIAL)

        // Then
        testHelpers!!.validatePostalResponse(
            postalVoteApplicationsMap = postalVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 1,
            apiResponse = responseSpec,
        )
    }
}
