package uk.gov.dluhc.emsintegrationapi.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingChecksAndDownloadsSummaryResponse
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingRegisterCheckSummary
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.UNAUTHORIZED_BEARER_TOKEN
import uk.gov.dluhc.emsintegrationapi.testsupport.bearerToken
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import java.time.Instant
import java.time.OffsetDateTime

internal class AdminGetPendingChecksAndDownloadsSummaryIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        private const val ADMIN_GET_PENDING_CHECKS_AND_DOWNLOADS_SUMMARY_ENDPOINT = "/admin/pending-checks-and-downloads-summary"
        private const val EXCLUDED_GSS_CODE = "E99999999"
        private const val GSS_CODE_1 = "E00000001"
        private const val GSS_CODE_2 = "E00000002"
    }

    @BeforeEach
    fun setup() {
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            proxyRepository = proxyVoteApplicationRepository,
        )
        wireMockService.stubCognitoAdminJwtIssuerResponse()
    }

    @Test
    fun `should return ok with empty summaries given no data`() {
        // When
        val response = webTestClient.get()
            .uri(ADMIN_GET_PENDING_CHECKS_AND_DOWNLOADS_SUMMARY_ENDPOINT)
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingChecksAndDownloadsSummaryResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingRegisterChecks).isEmpty()
        assertThat(actual.pendingPostalDownloads).isEmpty()
        assertThat(actual.pendingProxyDownloads).isEmpty()
    }

    @Test
    fun `should return summaries of pending register checks and downloads per gss code`() {
        // Given two register checks for gss code 1 have been pending since 2025-03-01T09:00:00Z and 2025-03-02T09:00:00Z
        saveRegisterCheckPendingSince(GSS_CODE_1, "2025-03-01 09:00:00")
        saveRegisterCheckPendingSince(GSS_CODE_1, "2025-03-02 09:00:00")

        // And gss code 1 had a successful register check at 2025-03-01T08:00:00Z
        registerCheckRepository.save(
            buildRegisterCheck(
                gssCode = GSS_CODE_1,
                status = CheckStatus.EXACT_MATCH,
                matchResultSentAt = Instant.parse("2025-03-01T08:00:00Z"),
            )
        )

        // And a register check for gss code 1 became pending less than a day ago, so is not counted
        registerCheckRepository.save(buildRegisterCheck(gssCode = GSS_CODE_1, status = CheckStatus.PENDING))

        // And gss code 2 has no pending register checks but had a successful register check at 2025-03-02T08:00:00Z
        registerCheckRepository.save(
            buildRegisterCheck(
                gssCode = GSS_CODE_2,
                status = CheckStatus.TOO_MANY_MATCHES,
                matchResultSentAt = Instant.parse("2025-03-02T08:00:00Z"),
            )
        )

        // And a register check for an excluded gss code has been pending since 2025-03-01T09:00:00Z, but is not reported
        saveRegisterCheckPendingSince(EXCLUDED_GSS_CODE, "2025-03-01 09:00:00")

        // And two postal applications for gss code 1, one with an EMS elector id, have been pending since 2025-03-01T10:00:00Z and 2025-03-02T10:00:00Z
        savePostalApplicationPendingSince(GSS_CODE_1, Instant.parse("2025-03-01T10:00:00Z"))
        savePostalApplicationPendingSince(GSS_CODE_1, Instant.parse("2025-03-02T10:00:00Z"), emsElectorId = null)

        // And a postal application for gss code 1 became pending less than 5 days ago, so is not counted
        postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.RECEIVED,
                applicationDetails = buildApplicationDetailsEntity(gssCode = GSS_CODE_1),
            )
        )

        // And a postal application for gss code 1 was successfully downloaded by the EMS at 2025-03-05T12:00:00Z
        savePostalApplicationDownloadedAt(GSS_CODE_1, "2025-03-05 12:00:00")

        // And a postal application for an excluded gss code has been pending since 2025-03-01T10:00:00Z, but is not reported
        savePostalApplicationPendingSince(EXCLUDED_GSS_CODE, Instant.parse("2025-03-01T10:00:00Z"))

        // And a proxy application for gss code 2 has been pending since 2025-03-03T10:00:00Z
        saveProxyApplicationPendingSince(GSS_CODE_2, Instant.parse("2025-03-03T10:00:00Z"))

        // And gss code 1 has no pending proxy applications but had a successful proxy download at 2025-03-06T12:00:00Z
        saveProxyApplicationDownloadedAt(GSS_CODE_1, "2025-03-06 12:00:00")

        // When
        val response = webTestClient.get()
            .uri(ADMIN_GET_PENDING_CHECKS_AND_DOWNLOADS_SUMMARY_ENDPOINT)
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingChecksAndDownloadsSummaryResponse::class.java)

        // Then
        val expected = AdminPendingChecksAndDownloadsSummaryResponse(
            pendingRegisterChecks = listOf(
                AdminPendingRegisterCheckSummary(
                    gssCode = GSS_CODE_1,
                    registerCheckCount = 2,
                    earliestDateCreated = OffsetDateTime.parse("2025-03-01T09:00:00Z"),
                    latestMatchResultSentAt = OffsetDateTime.parse("2025-03-01T08:00:00Z"),
                ),
                AdminPendingRegisterCheckSummary(
                    gssCode = GSS_CODE_2,
                    registerCheckCount = 0,
                    earliestDateCreated = null,
                    latestMatchResultSentAt = OffsetDateTime.parse("2025-03-02T08:00:00Z"),
                ),
            ),
            pendingPostalDownloads = listOf(
                AdminPendingEmsDownloadSummary(
                    gssCode = GSS_CODE_1,
                    pendingDownloadCount = 2,
                    pendingDownloadCountWithEmsElectorId = 1,
                    earliestDateCreated = OffsetDateTime.parse("2025-03-01T10:00:00Z"),
                    lastSuccessfulEmsDownload = OffsetDateTime.parse("2025-03-05T12:00:00Z"),
                ),
            ),
            pendingProxyDownloads = listOf(
                AdminPendingEmsDownloadSummary(
                    gssCode = GSS_CODE_1,
                    pendingDownloadCount = 0,
                    pendingDownloadCountWithEmsElectorId = 0,
                    earliestDateCreated = null,
                    lastSuccessfulEmsDownload = OffsetDateTime.parse("2025-03-06T12:00:00Z"),
                ),
                AdminPendingEmsDownloadSummary(
                    gssCode = GSS_CODE_2,
                    pendingDownloadCount = 1,
                    pendingDownloadCountWithEmsElectorId = 1,
                    earliestDateCreated = OffsetDateTime.parse("2025-03-03T10:00:00Z"),
                    lastSuccessfulEmsDownload = null,
                ),
            ),
        )
        val actual = response.responseBody.blockFirst()
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun `should include recently created pending checks and downloads given minimum age params of zero days`() {
        // Given a register check for gss code 1 became pending less than a day ago
        registerCheckRepository.save(buildRegisterCheck(gssCode = GSS_CODE_1, status = CheckStatus.PENDING))

        // And a postal application for gss code 1 became pending less than 5 days ago
        postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.RECEIVED,
                applicationDetails = buildApplicationDetailsEntity(gssCode = GSS_CODE_1),
            )
        )

        // And a proxy application for gss code 2 became pending less than 5 days ago
        proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.RECEIVED,
                applicationDetails = buildApplicationDetailsEntity(gssCode = GSS_CODE_2),
            )
        )

        // When the summary is requested with minimum pending ages of 0 days
        val response = webTestClient.get()
            .uri(
                UriComponentsBuilder
                    .fromUriString(ADMIN_GET_PENDING_CHECKS_AND_DOWNLOADS_SUMMARY_ENDPOINT)
                    .queryParam("registerChecksPendingMinAgeInDays", 0)
                    .queryParam("emsDownloadsPendingMinAgeInDays", 0)
                    .build().toUriString()
            )
            .bearerToken(getBearerToken())
            .exchange()
            .expectStatus().isOk
            .returnResult(AdminPendingChecksAndDownloadsSummaryResponse::class.java)

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertThat(actual!!.pendingRegisterChecks.map { it.gssCode to it.registerCheckCount })
            .containsExactly(GSS_CODE_1 to 1)
        assertThat(actual.pendingPostalDownloads.map { it.gssCode to it.pendingDownloadCount })
            .containsExactly(GSS_CODE_1 to 1)
        assertThat(actual.pendingProxyDownloads.map { it.gssCode to it.pendingDownloadCount })
            .containsExactly(GSS_CODE_2 to 1)
    }

    @Test
    fun `should return unauthorized given authentication token is invalid`() {
        // When, Then
        webTestClient.get()
            .uri(ADMIN_GET_PENDING_CHECKS_AND_DOWNLOADS_SUMMARY_ENDPOINT)
            .bearerToken(UNAUTHORIZED_BEARER_TOKEN)
            .exchange()
            .expectStatus().isUnauthorized
            .returnResult(ErrorResponse::class.java)
    }

    /**
     * The date_created field on the entity has the @CreatedDate annotation so is overwritten when first saved.
     * We update the field with SQL after creating the register check to bypass this.
     */
    private fun saveRegisterCheckPendingSince(gssCode: String, dateCreated: String): RegisterCheck {
        val registerCheck = registerCheckRepository.save(buildRegisterCheck(gssCode = gssCode, status = CheckStatus.PENDING))
        jdbcTemplate.execute("UPDATE register_check SET date_created = '$dateCreated' WHERE id = '${registerCheck.id}'")
        return registerCheck
    }

    private fun savePostalApplicationPendingSince(
        gssCode: String,
        dateCreated: Instant,
        emsElectorId: String? = "an-ems-elector-id",
    ): PostalVoteApplication {
        val application = buildPostalVoteApplication(
            recordStatus = RecordStatus.RECEIVED,
            applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            applicantDetails = buildApplicantDetailsEntity(emsElectorId = emsElectorId),
        )
        // Application needs to be saved first to avoid the date created being overwritten
        postalVoteApplicationRepository.save(application)
        application.dateCreated = dateCreated
        return postalVoteApplicationRepository.save(application)
    }

    private fun saveProxyApplicationPendingSince(
        gssCode: String,
        dateCreated: Instant,
        emsElectorId: String? = "an-ems-elector-id",
    ): ProxyVoteApplication {
        val application = buildProxyVoteApplication(
            recordStatus = RecordStatus.RECEIVED,
            applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            applicantDetails = buildApplicantDetailsEntity(emsElectorId = emsElectorId),
        )
        // Application needs to be saved first to avoid the date created being overwritten
        proxyVoteApplicationRepository.save(application)
        application.dateCreated = dateCreated
        return proxyVoteApplicationRepository.save(application)
    }

    /**
     * The date_updated field is managed by the auditing entity listener so cannot be set by saving the entity.
     * We update the field with SQL to bypass this.
     */
    private fun savePostalApplicationDownloadedAt(gssCode: String, dateUpdated: String) {
        val application = postalVoteApplicationRepository.save(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.DELETED,
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        )
        jdbcTemplate.execute(
            "UPDATE postal_vote_application SET date_updated = '$dateUpdated' WHERE application_id = '${application.applicationId}'"
        )
    }

    private fun saveProxyApplicationDownloadedAt(gssCode: String, dateUpdated: String) {
        val application = proxyVoteApplicationRepository.save(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.DELETED,
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        )
        jdbcTemplate.execute(
            "UPDATE proxy_vote_application SET date_updated = '$dateUpdated' WHERE application_id = '${application.applicationId}'"
        )
    }
}
