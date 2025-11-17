package uk.gov.dluhc.emsintegrationapi.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.UNAUTHORIZED_BEARER_TOKEN
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.models.ErrorResponseAssert.Companion.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.bearerToken
import uk.gov.dluhc.emsintegrationapi.testsupport.getRandomGssCode
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import uk.gov.dluhc.registercheckerapi.models.AdminPendingRegisterChecksResponse
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse

internal class AdminGetPendingRegisterChecksIntegrationTest : IntegrationTest() {

    companion object {
        private const val ADMIN_GET_PENDING_REGISTER_CHECKS_ENDPOINT = "/admin/pending-checks/"
    }

    @BeforeEach
    fun setup() {
        wireMockService.stubCognitoAdminJwtIssuerResponse()
    }

    @Test
    fun `should return ok with empty pending register checks`() {
        // Given
        val eroId = "south-testington"
        val gssCode = getRandomGssCode()

        wireMockService.stubIerApiGetEros("", eroId, listOf(gssCode))

        // When
        val response = webTestClient.get()
            .uri(buildUri(eroId))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingRegisterChecksResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingRegisterChecks).isEmpty()
        wireMockService.verifyIerGetErosCalledOnce()
    }

    @Test
    fun `should return ok with multiple pending register checks`() {
        // Given
        val eroId = "north-testington"
        val firstGssCode = getRandomGssCode()
        val secondGssCode = getRandomGssCode()
        val gssCodes = listOf(firstGssCode, secondGssCode)
        val expectedRecordCount = 3

        wireMockService.stubIerApiGetEros("", eroId, gssCodes)

        val pendingCheck1 = registerCheckRepository.save(buildRegisterCheck(gssCode = firstGssCode, status = CheckStatus.PENDING))
        Thread.sleep(1000)
        val pendingCheck2 = registerCheckRepository.save(buildRegisterCheck(gssCode = firstGssCode, status = CheckStatus.PENDING))
        Thread.sleep(1000)
        val pendingCheck3 = registerCheckRepository.save(buildRegisterCheck(gssCode = secondGssCode, status = CheckStatus.PENDING))

        // When
        val response = webTestClient.get()
            .uri(buildUri(eroId))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingRegisterChecksResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingRegisterChecks).hasSize(expectedRecordCount)
        assertThat(actual.pendingRegisterChecks.map { it.applicationId }).isEqualTo(
            listOf(pendingCheck1.sourceReference, pendingCheck2.sourceReference, pendingCheck3.sourceReference)
        )
    }

    @Test
    fun `should return not found error given IER service returns no matching ERO`() {
        val eroId = "east-testington"
        val gssCode = getRandomGssCode()

        wireMockService.stubIerApiGetEros("", "west-testington", listOf(gssCode))

        // When
        val response = webTestClient.get()
            .uri(buildUri(eroId))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().is4xxClientError
            .returnResult(ErrorResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual)
            .hasStatus(404)
            .hasError("Not Found")
            .hasMessage("ERO with eroId=[$eroId] not found")
    }

    @Test
    fun `should return internal server error given IER service fails`() {
        // Given
        wireMockService.stubIerApiGetEroIdentifierThrowsInternalServerError()

        // When
        val response = webTestClient.get()
            .uri(buildUri("west-testington"))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().is5xxServerError
            .returnResult(ErrorResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual)
            .hasStatus(500)
            .hasError("Internal Server Error")
            .hasMessage("Error retrieving EROs from IER API")
    }

    @Test
    fun `should return unauthorized given bearer token is invalid`() {
        // When, Then
        webTestClient.get()
            .uri(buildUri("west-testington"))
            .bearerToken(UNAUTHORIZED_BEARER_TOKEN)
            .exchange()
            .expectStatus().isUnauthorized
            .returnResult(ErrorResponse::class.java)
    }

    private fun buildUri(eroId: String) =
        UriComponentsBuilder
            .fromUriString(ADMIN_GET_PENDING_REGISTER_CHECKS_ENDPOINT)
            .path(eroId)
            .build().toUriString()
}
