package uk.gov.dluhc.emsintegrationapi.testsupport

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.mockito.kotlin.atLeastOnce
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.config.CORRELATION_ID_HEADER
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildElectoralRegistrationOfficeResponse
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerLocalAuthorityDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildLocalAuthorityResponse
import uk.gov.dluhc.external.ier.models.ERODetails
import uk.gov.dluhc.external.ier.models.ErosGet200Response

private const val IER_EROS_GET_URL = "/ier-ero/eros"
private const val IER_ERO_GET_URL = "/ier-ero/.*"
private const val ERO_MANAGEMENT_ERO_GET_URL = "/ero-management-api/eros/.*"

@Service
class WiremockService(private val wireMockServer: WireMockServer) {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun resetAllStubsAndMappings() {
        wireMockServer.resetAll()
    }

    fun verifyIerGetEroIdentifierCalledOnce() {
        verifyIerGetEroIdentifierCalled(1)
    }

    fun verifyEroManagementGetEroIdentifierCalledOnce() {
        verifyEroManagementGetEroIdentifierCalled(1)
    }

    fun verifyIerGetEroIdentifierCalled(count: Int) {
        wireMockServer.verify(count, getRequestedFor(urlPathMatching(IER_ERO_GET_URL)))
    }

    fun verifyIerGetEroIdentifierNeverCalled() {
        verifyIerGetEroIdentifierCalled(0)
    }

    fun stubIerApiGetEros(certificateSerial: String, eroId: String, gssCodes: List<String>) {
        val erosResponse = ErosGet200Response(
            eros = listOf(
                buildIerEroDetails(
                    eroIdentifier = eroId,
                    activeClientCertificateSerials = listOf(certificateSerial),
                    localAuthorities = gssCodes.map { buildIerLocalAuthorityDetails(gssCode = it) },
                )
            )
        )

        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(erosResponse))
                )
        )
    }

    fun stubIerInternalServerError() {
        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(500)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        )
    }

    fun stubIerApiGetEros(eros: List<ERODetails>) {
        val erosResponse = ErosGet200Response(eros = eros)

        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(erosResponse))
                )
        )
    }

    fun stubIerApiGetEroIdentifier(certificateSerial: String, eroId: String) {
        wireMockServer.stubFor(
            get(urlEqualTo(buildGetIerEndpointUrl(certificateSerial)))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                                {
                                    "eroId": "$eroId",
                                    "certificateSerial": "$certificateSerial"
                                }
                            """.trimIndent()
                        )
                )
        )
    }

    fun stubIerApiGetEroIdentifierThrowsInternalServerError() =
        stubIerApiGetEroIdentifierThrowsException(500)

    fun stubIerApiGetEroIdentifierThrowsInternalServerError(certificateSerial: String) =
        stubIerApiGetEroIdentifierThrowsException(certificateSerial, 500)

    fun stubIerApiGetEroIdentifierThrowsNotFoundError() =
        stubIerApiGetEroIdentifierThrowsException(404)

    fun stubIerApiGetEroIdentifierThrowsNotFoundError(certificateSerial: String) =
        stubIerApiGetEroIdentifierThrowsException(certificateSerial, 404)

    fun verifyEroManagementGetEroIdentifierCalled(count: Int) {
        wireMockServer.verify(count, getRequestedFor(urlPathMatching(ERO_MANAGEMENT_ERO_GET_URL)))
    }

    fun stubEroManagementGetEroThrowsNotFoundError(eroId: String) {
        stubEroManagementGetEroThrowsException(eroId, 404)
    }

    fun stubEroManagementGetEroThrowsInternalServerError(eroId: String) {
        stubEroManagementGetEroThrowsException(eroId, 500)
    }

    fun stubEroManagementGetEroThrowsException(
        eroId: String,
        httpStatusCode: Int,
        message: String = "Error"
    ) {
        wireMockServer.stubFor(
            get(urlPathMatching("/ero-management-api/eros/$eroId"))
                .willReturn(
                    responseDefinition()
                        .withStatus(httpStatusCode)
                        .withBody(message)
                )
        )
    }

    fun verifyWiremockGetInvokedFor(certificateSerial: String) {
        wireMockServer.verify(
            getRequestedFor(urlPathMatching("/ier-ero/ero"))
                .withQueryParam("certificateSerial", WireMock.equalTo(certificateSerial))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
        )
    }

    fun stubEroManagementGetEro(eroId: String = "1234", gssCode1: String = "E12345678", gssCode2: String = "E98765432") {
        val eroResponse = buildElectoralRegistrationOfficeResponse(
            eroId = eroId,
            localAuthorities = mutableListOf(
                buildLocalAuthorityResponse(gssCode = gssCode1),
                buildLocalAuthorityResponse(gssCode = gssCode2)
            )
        )
        wireMockServer.stubFor(
            get(urlPathEqualTo("/ero-management-api/eros/$eroId"))
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eroResponse))
                )
        )
    }

    fun verifyIerApiRequestWithCorrelationId(correlationId: String) {
        wireMockServer.verify(
            getRequestedFor(urlPathMatching(IER_ERO_GET_URL))
                .withHeader(CORRELATION_ID_HEADER, equalTo(correlationId))
        )
    }

    fun verifyIerGetErosNeverCalled() {
        verifyIerGetErosCalled(0)
    }

    fun verifyIerGetErosCalled(count: Int) {
        wireMockServer.verify(count, getRequestedFor(urlPathMatching(IER_EROS_GET_URL)))
    }

    fun verifyIerGetErosCalledOnce() {
        verifyIerGetErosCalled(1)
    }

    fun verifyEroManagementApiRequestWithCorrelationId(correlationId: String) {
        wireMockServer.verify(
            getRequestedFor(urlPathMatching(ERO_MANAGEMENT_ERO_GET_URL))
                .withHeader(CORRELATION_ID_HEADER, equalTo(correlationId))
        )
    }

    private fun stubIerApiGetEroIdentifierThrowsException(httpStatusCode: Int) {
        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(httpStatusCode)
                )
        )
    }

    private fun stubIerApiGetEroIdentifierThrowsException(
        certificateSerial: String,
        httpStatusCode: Int,
        message: String = "Error"
    ) {
        wireMockServer.stubFor(
            get(urlEqualTo(buildGetIerEndpointUrl(certificateSerial)))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(httpStatusCode)
                        .withBody(message)
                )
        )
    }

    private fun matchingAwsSignedAuthHeader(): StringValuePattern =
        matching(
            "AWS4-HMAC-SHA256 " +
                "Credential=.*, " +
                "SignedHeaders=accept-encoding;host;x-amz-content-sha256;x-amz-date;x-amz-security-token;x-correlation-id, " +
                "Signature=.*"
        )

    private fun buildGetIerEndpointUrl(certificateSerial: String) = "/ier-ero/ero?certificateSerial=$certificateSerial"
}
