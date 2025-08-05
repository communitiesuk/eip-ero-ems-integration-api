package uk.gov.dluhc.emsintegrationapi.rest.proxy

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
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_99
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_INVALID
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.ProxyIntegrationTestHelpers
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import java.time.Clock
import java.time.ZoneOffset

internal class GetProxyVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    private lateinit var clock: TestClockConfiguration.FlexibleClock

    @Autowired
    private lateinit var eroAbsentVoteHoldRepository: EroAbsentVoteHoldRepository

    private var apiClient: ApiClient? = null

    private var fixtures: ProxyIntegrationTestHelpers? = null

    private val acceptedPath = "/proxyvotes"

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        eroAbsentVoteHoldRepository.deleteAll()
        ClearDownUtils.clearDownRecords(
            proxyRepository = proxyVoteApplicationRepository,
            registerCheckResultDataRepository = registerCheckResultDataRepository
        )
        apiClient = ApiClient(webClient, apiProperties)
        fixtures =
            ProxyIntegrationTestHelpers(
                wiremockService = wireMockService,
                proxyVoteApplicationRepository = proxyVoteApplicationRepository,
                queueMessagingTemplate = sqsMessagingTemplate,
                eroAbsentVoteHoldRepository = eroAbsentVoteHoldRepository
            )
        fixtures!!.givenEroIdAndGssCodesMapped()
        val timeBeforeThreshold = apiProperties.holdingPoolThresholdDate.minusSeconds(3600)
        clock.setClock(Clock.fixed(timeBeforeThreshold, ZoneOffset.UTC))
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When I send a get proxy vote applications request without a certificate serial number in the request header
        val responseSpec = apiClient!!.get(acceptedPath, attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the page size is greater than the configured page size 50`() {
        // When I send a get proxy vote applications request with the page size 51 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                51,
            )

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "The page size must be greater than or equal to 1 and less than or equal to 50"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            errorResponse!!.message,
        ).isEqualTo("getProxyVoteApplications.pageSize: The page size must be greater than or equal to 1 and less than or equal to 50")
    }

    @Test
    fun `System does not have any proxy vote applications`() {
        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                10,
            )

        // Then I received a response with 0 proxy vote applications with signature
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = mapOf(),
            hasSignature = true,
            expectedPageSize = 0,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System returns http status 404 if the attached certificate serial number does not exist`() {
        // Given the certificate serial CERTIFICATE_SERIAL_NUM_INVALID does not exist in ERO
        wireMockService.stubIerApiGetEroIdentifierThrowsNotFoundError(CERTIFICATE_SERIAL_NUM_INVALID)

        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_INVALID
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                "/proxyvotes",
                CERTIFICATE_SERIAL_NUM_INVALID,
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The EROCertificateMapping for certificateSerial=[CERTIFICATE_SERIAL_NUM_INVALID] could not be found"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            errorResponse!!.message,
        ).isEqualTo("EROCertificateMapping for certificateSerial=[$CERTIFICATE_SERIAL_NUM_INVALID] not found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get mapping request`() {
        // Given the ERO could not process the get mapping request for CERTIFICATE_SERIAL_NUM_99
        wireMockService.stubIerInternalServerError()

        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_99
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_99,
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And it has an error message of "Error retrieving EROs from IER API"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            errorResponse!!.message,
        ).isEqualTo(
            "Error retrieving EROs from IER API",
        )
    }

    @Test
    fun `System returns http status 404 if ERO Mapping Id does not exist`() {
        // Given the ERO Id "camden-city-council" does not exist in ERO
        wireMockService.stubIerApiGetNoEros()

        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                10,
            )

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "EROCertificateMapping for certificateSerial=[1234567891] not found"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(errorResponse!!.message).isEqualTo("EROCertificateMapping for certificateSerial=[1234567891] not found")
    }

    @Test
    fun `System returns http status 500 if ERO could not process the get gss code request`() {
        // Given the ERO could not process the get gss codes request for "camden-city-council"
        wireMockService.stubIerInternalServerError()

        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                10,
            )

        // Then I received the http status 500
        responseSpec.expectStatus().is5xxServerError

        // And it has an error message of "Error retrieving EROs from IER API"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(message!!.message).isEqualTo("Error retrieving EROs from IER API")
    }

    @Test
    fun `System returns proxy vote applications of a given page size`() {
        // Given there are 20 proxy vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val proxyVoteApplicationMap = fixtures!!.buildProxyVoteApplications(20, "RECEIVED", "E12345678", "E12345679")

        // When I send a get proxy vote applications request with the page size 10 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                10,
            )

        // Then I received a response with 10 proxy vote applications with signature
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 10,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System does not have requested number of proxy applications`() {
        // Given there are 2 proxy vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val proxyVoteApplicationMap = fixtures!!.buildProxyVoteApplications(2, "RECEIVED", "E12345678", "E12345679")

        // When I send a get proxy vote applications request with the page size 3 and the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
                3,
            )

        // Then I received a response with 2 proxy vote applications with signature
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 2,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System returns default number of records if page size is not specified`() {
        // Given there are 21 proxy vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val proxyVoteApplicationMap = fixtures!!.buildProxyVoteApplications(21, "RECEIVED", "E12345678", "E12345679")

        // When I send a get proxy vote request without the page size and with the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
            )

        // Then I received a response with 20 proxy vote applications with signature
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 20,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System returns proxy vote applications with signature waiver reason`() {
        // Given there are 21 proxy vote applications without signature exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
        val proxyVoteApplicationMap =
            fixtures!!.buildProxyVoteApplications(
                numberOfRecords = 21,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf("E12345678", "E12345679"),
                signatureBase64 = null,
                signatureWaived = true,
                signatureWaiverReason = SIGNATURE_WAIVER_REASON,
            )

        // When I send a get proxy vote request without the page size and with the certificate serial number CERTIFICATE_SERIAL_NUM_1
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
                apiClient!!,
                acceptedPath,
                CERTIFICATE_SERIAL_NUM_1,
            )

        // Then I received a response with 20 proxy vote applications with signature waiver
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = false,
            expectedPageSize = 20,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System does not return proxy vote applications if hold is enabled and threshold date has passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.plusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        fixtures!!.givenEroIdAndGssCodesMapped()
        fixtures!!.createEroAbsentVoteHold(eroId = "camden-city-council", holdEnabled = true)

        fixtures!!.buildProxyVoteApplications(
            numberOfRecords = 10,
            recordStatus = "RECEIVED",
            gssCodes = arrayOf("E12345678"),
        )

        // When
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, CERTIFICATE_SERIAL_NUM_1)

        // Then
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = mapOf(),
            hasSignature = true,
            expectedPageSize = 0,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System returns proxy vote applications if hold not enabled and threshold date has passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.plusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        fixtures!!.givenEroIdAndGssCodesMapped()
        fixtures!!.createEroAbsentVoteHold(eroId = "camden-city-council", holdEnabled = false)

        val proxyVoteApplicationMap =
            fixtures!!.buildProxyVoteApplications(
                numberOfRecords = 1,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf("E12345678"),
            )

        // When
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, CERTIFICATE_SERIAL_NUM_1)

        // Then
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 1,
            responseSpec = responseSpec,
        )
    }

    @Test
    fun `System returns proxy vote applications if hold enabled and threshold date has not passed`() {
        // Given
        val timeAfterThreshold = apiProperties.holdingPoolThresholdDate.minusSeconds(3600)
        clock.setClock(Clock.fixed(timeAfterThreshold, ZoneOffset.UTC))

        fixtures!!.givenEroIdAndGssCodesMapped()
        fixtures!!.createEroAbsentVoteHold(eroId = "camden-city-council", holdEnabled = true)

        val proxyVoteApplicationMap =
            fixtures!!.buildProxyVoteApplications(
                numberOfRecords = 1,
                recordStatus = "RECEIVED",
                gssCodes = arrayOf("E12345678"),
            )

        // When
        val responseSpec =
            fixtures!!.sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(apiClient!!, acceptedPath, CERTIFICATE_SERIAL_NUM_1)

        // Then
        fixtures!!.validateProxyResponse(
            proxyVoteApplicationsMap = proxyVoteApplicationMap,
            hasSignature = true,
            expectedPageSize = 1,
            responseSpec = responseSpec,
        )
    }
}
