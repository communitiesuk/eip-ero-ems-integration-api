package uk.gov.dluhc.emsintegrationapi.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadsResponse
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.UNAUTHORIZED_BEARER_TOKEN
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.models.ErrorResponseAssert.Companion.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.bearerToken
import uk.gov.dluhc.emsintegrationapi.testsupport.getRandomGssCode
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse

internal class AdminGetPendingEmsDownloadsIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    companion object {
        private const val ADMIN_GET_PENDING_EMS_DOWNLOADS_ENDPOINT = "/admin/pending-downloads/"
    }

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            proxyRepository = proxyVoteApplicationRepository
        )
        wireMockService.stubCognitoAdminJwtIssuerResponse()
    }

    @Test
    fun `should return ok with empty pending downloads`() {
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
            .returnResult(AdminPendingEmsDownloadsResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingEmsDownloads).isEmpty()
        wireMockService.verifyIerGetErosCalledOnce()
    }

    @Test
    fun `should return ok with multiple pending postal and proxy downloads across multiple GSS codes`() {
        // Given
        val eroId = "south-testington"
        val firstGssCode = getRandomGssCode()
        val secondGssCode = getRandomGssCode()
        val excludedGssCode = getRandomGssCode()

        wireMockService.stubIerApiGetEros("", eroId, listOf(firstGssCode, secondGssCode))

        val pendingDownload1 = postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = firstGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )
        Thread.sleep(1000)
        val pendingDownload2 = proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = secondGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )
        Thread.sleep(1000)
        val pendingDownload3 = proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = firstGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )
        Thread.sleep(1000)
        val pendingDownload4 = postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = secondGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )
        val downloadedPostalDownload = postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = firstGssCode),
                recordStatus = RecordStatus.DELETED
            )
        )
        val downloadedProxyDownload = proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = firstGssCode),
                recordStatus = RecordStatus.DELETED
            )
        )
        val excludedPendingPostalDownload = postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = excludedGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )
        val excludedPendingProxyDownload = proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(gssCode = excludedGssCode),
                recordStatus = RecordStatus.RECEIVED
            )
        )

        println(pendingDownload1.dateCreated)
        println(pendingDownload2.dateCreated)
        println(pendingDownload3.dateCreated)
        println(pendingDownload4.dateCreated)

        val expectedPendingDownloads = listOf(
            pendingDownload1.applicationId,
            pendingDownload2.applicationId,
            pendingDownload3.applicationId,
            pendingDownload4.applicationId
        )

        // When
        val response = webTestClient.get()
            .uri(buildUri(eroId))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingEmsDownloadsResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingEmsDownloads).hasSize(expectedPendingDownloads.size)
        assertThat(actual.pendingEmsDownloads.map { it.applicationId }).isEqualTo(expectedPendingDownloads)
    }

    @Test
    fun `should return not found error given IER service returns no matching ERO`() {
        // Given
        val eroId = "south-testington"
        wireMockService.stubIerApiGetNoEros()

        // When
        val response = webTestClient.get()
            .uri(buildUri(eroId))
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isNotFound
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
            .uri(buildUri("south-testington"))
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
    fun `should return unauthorized given authentication token is invalid`() {
        // When, Then
        webTestClient.get()
            .uri(buildUri("south-testington"))
            .bearerToken(UNAUTHORIZED_BEARER_TOKEN)
            .exchange()
            .expectStatus().isUnauthorized
            .returnResult(ErrorResponse::class.java)
    }

    private fun buildUri(eroId: String) =
        UriComponentsBuilder
            .fromUriString(ADMIN_GET_PENDING_EMS_DOWNLOADS_ENDPOINT)
            .path(eroId)
            .build().toUriString()
}
