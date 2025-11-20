package uk.gov.dluhc.emsintegrationapi.testsupport

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.config.CORRELATION_ID_HEADER
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerLocalAuthorityDetails
import uk.gov.dluhc.external.ier.models.ERODetails
import uk.gov.dluhc.external.ier.models.ErosGet200Response

private const val IER_EROS_GET_URL = "/ier-ero/eros"
private const val IER_ERO_GET_URL = "/ier-ero/.*"

@Service
class WiremockService(
    private val wireMockServer: WireMockServer,
) {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun resetAllStubsAndMappings() {
        wireMockServer.resetAll()
    }

    fun verifyIerGetEroIdentifierCalledOnce() {
        verifyIerGetEroIdentifierCalled(1)
    }

    fun verifyIerGetEroIdentifierCalled(count: Int) {
        wireMockServer.verify(count, getRequestedFor(urlPathMatching(IER_ERO_GET_URL)))
    }

    fun stubIerApiGetEros(
        certificateSerial: String,
        eroId: String,
        gssCodes: List<String>,
    ) {
        val erosResponse =
            ErosGet200Response(
                eros =
                listOf(
                    buildIerEroDetails(
                        eroIdentifier = eroId,
                        activeClientCertificateSerials = listOf(certificateSerial),
                        localAuthorities = gssCodes.map { buildIerLocalAuthorityDetails(gssCode = it) },
                    ),
                ),
            )

        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(erosResponse)),
                ),
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
                        .withBody(""),
                ),
        )
    }

    fun stubIerApiGetNoEros() {
        val erosResponse = ErosGet200Response(eros = listOf())

        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(erosResponse)),
                ),
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
                        .withBody(objectMapper.writeValueAsString(erosResponse)),
                ),
        )
    }

    fun stubIerApiGetEroIdentifier(
        certificateSerial: String,
        eroId: String,
    ) {
        stubIerApiGetEros(
            listOf(
                buildIerEroDetails(
                    eroIdentifier = eroId,
                    name = "Camden City Council",
                    gssCode = "E12345678",
                    activeClientCertificateSerials = listOf(certificateSerial),
                ),
            ),
        )
    }

    fun stubIerApiGetErosWithGssCodes(
        certificateSerial: String,
        eroId: String,
        gssCodes: List<String>,
    ) {
        stubIerApiGetEros(
            gssCodes.map { gssCode ->
                buildIerEroDetails(
                    eroIdentifier = eroId,
                    gssCode = gssCode,
                    activeClientCertificateSerials = listOf(certificateSerial),
                )
            },
        )
    }

    fun stubIerApiGetEroIdentifierThrowsInternalServerError() {
        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(500),
                ),
        )
    }

    fun stubIerApiGetEroIdentifierThrowsNotFoundError(certificateSerial: String) =
        stubIerApiGetEroIdentifierThrowsException(certificateSerial, 404)

    fun verifyIerApiRequestWithCorrelationId(correlationId: String) {
        wireMockServer.verify(
            getRequestedFor(urlPathMatching(IER_ERO_GET_URL))
                .withHeader(CORRELATION_ID_HEADER, equalTo(correlationId)),
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

    fun givenEroIdAndGssCodesMappedToCertificate(
        certificateSerialNumber: String,
        eroId: String,
    ) {
        // Given the certificate serial number $certificateSerialNumber mapped to the ERO Id $eroId
        // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
        val gssCodes = listOf("E12345678", "E12345679")
        givenEroIdAndGssCodesMappedToCertificate(certificateSerialNumber, eroId, gssCodes)
    }

    fun givenEroIdAndGssCodesMappedToCertificate(
        certificateSerialNumber: String,
        eroId: String,
        gssCodes: List<String>,
    ) {
        // Given the certificate serial number $certificateSerialNumber mapped to the ERO Id $eroId
        // And the gss codes $gssCodes.. mapped to the ERO Id
        stubIerApiGetEros(
            listOf(
                buildIerEroDetails(
                    eroIdentifier = eroId,
                    localAuthorities =
                    gssCodes.map { gssCode ->
                        buildIerLocalAuthorityDetails(gssCode = gssCode)
                    },
                    activeClientCertificateSerials = listOf(certificateSerialNumber),
                ),
            ),
        )
    }

    private val cognitoRsaJwkKeyPair = RsaKeyPair.jwk.toJSONString()

    fun stubCognitoAdminJwtIssuerResponse() {
        wireMockServer.stubFor(
            get(urlPathMatching("/cognito/admin/.well-known/jwks.json"))
                .willReturn(
                    WireMock.ok().withBody(
                        """
                        {
                           "keys":[$cognitoRsaJwkKeyPair]
                        }
                        """.trimIndent()
                    )
                )
        )
    }

    private fun stubIerApiGetEroIdentifierThrowsException(httpStatusCode: Int) {
        wireMockServer.stubFor(
            get(urlEqualTo(IER_EROS_GET_URL))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(httpStatusCode),
                ),
        )
    }

    private fun stubIerApiGetEroIdentifierThrowsException(
        certificateSerial: String,
        httpStatusCode: Int,
        message: String = "Error",
    ) {
        wireMockServer.stubFor(
            get(urlEqualTo(buildGetIerEndpointUrl(certificateSerial)))
                .withHeader("Authorization", matchingAwsSignedAuthHeader())
                .willReturn(
                    responseDefinition()
                        .withStatus(httpStatusCode)
                        .withBody(message),
                ),
        )
    }

    private fun matchingAwsSignedAuthHeader(): StringValuePattern =
        matching(
            "AWS4-HMAC-SHA256 " +
                "Credential=.*, " +
                "SignedHeaders=accept-encoding;host;x-amz-content-sha256;x-amz-date;x-amz-security-token;x-correlation-id, " +
                "Signature=.*",
        )

    private fun buildGetIerEndpointUrl(certificateSerial: String) = "/ier-ero/eros?certificateSerial=$certificateSerial"
}
