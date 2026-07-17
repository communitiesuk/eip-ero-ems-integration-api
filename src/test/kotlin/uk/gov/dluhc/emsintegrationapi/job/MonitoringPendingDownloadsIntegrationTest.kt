package uk.gov.dluhc.emsintegrationapi.job

import ch.qos.logback.classic.Level
import org.apache.commons.lang3.StringUtils.deleteWhitespace
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.LocalstackEmailMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.buildLocalstackEmailMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import java.time.Instant
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

internal class MonitoringPendingDownloadsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    private lateinit var pendingDownloadsMonitoringJob: PendingDownloadsMonitoringJob

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        TestLogAppender.reset()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(
            proxyRepository = proxyVoteApplicationRepository,
            postalRepository = postalVoteApplicationRepository,
            registerCheckResultDataRepository = registerCheckResultDataRepository,
        )
    }

    @AfterEach
    fun reset() {
        TestLogAppender.reset()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(
            proxyRepository = proxyVoteApplicationRepository,
            postalRepository = postalVoteApplicationRepository,
            registerCheckResultDataRepository = registerCheckResultDataRepository,
        )
    }

    @Test
    fun `System does not report on pending postal downloads that are less than 5 days old`() {
        // Given a postal vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000001", 3, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System does not report on postal downloads that have been deleted`() {
        // Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
        createPostalApplicationWithEmsElectorId("E00000001", 6, "DELETED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System does not report on pending postal downloads from excluded GSS codes`() {
        // Given a postal vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E99999999", 6, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 postal applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System reports on postal downloads that have been pending for more than 5 days, grouped by gss code`() {
        // Given a postal vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000001", 6, "RECEIVED")

        // And a postal vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000001", 7, "RECEIVED")

        // And a postal vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000001", 8, "RECEIVED")

        // And a postal vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000002", 6, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 4 postal applications (4 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 4 postal applications (4 with EMS Elector Ids) have been pending for more than PT120H.")

        // And the message "The gss code E00000001 has 3 postal applications (3 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000001 has 3 postal applications \(3 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )

        // And the message "The gss code E00000002 has 1 postal applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000002 has 1 postal applications \(1 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )
    }

    @Test
    fun `System reports number of pending postal downloads with an EMS Elector ID separately`() {
        // Given a postal vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createPostalApplicationWithEmsElectorId("E00000001", 6, "RECEIVED")

        // And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
        setDateCreatedToDaysAgoPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            7,
        )

        // And a postal vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 8 days ago and has record status "RECEIVED"
        setDateCreatedToDaysAgoPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            8,
        )

        // And a postal vote application with gss code "E00000002" without an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        val postalVoteApplication =
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000002"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            )
        setDateCreatedToDaysAgoPostal(postalVoteApplication, 6)

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 4 postal applications (1 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 4 postal applications (1 with EMS Elector Ids) have been pending for more than PT120H.")

        // And the message "The gss code E00000001 has 3 postal applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000001 has 3 postal applications \(1 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )

        // And the message "The gss code E00000002 has 1 postal applications (0 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000002 has 1 postal applications \(0 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )
    }

    @Test
    fun `System does not report on pending proxy downloads that are less than 5 days old`() {
        // Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 3 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000001", 3, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System does not report on proxy downloads that have been deleted`() {
        // Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "DELETED"
        createProxyApplicationWithEmsElectorId("E00000001", 3, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System does not report on pending proxy downloads from excluded GSS codes`() {
        // Given a proxy vote application with gss code "E99999999" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E99999999", 6, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 0 proxy applications (0 with EMS Elector Ids) have been pending for more than PT120H.")
    }

    @Test
    fun `System reports on proxy downloads that have been pending for more than 5 days, grouped by gss code`() {
        // Given a proxy vote application with gss code "E00000001" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000001", 6, "RECEIVED")

        // And a proxy vote application with gss code "E00000001" was saved to EMS integration API 7 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000001", 7, "RECEIVED")

        // And a proxy vote application with gss code "E00000001" was saved to EMS integration API 8 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000001", 8, "RECEIVED")

        // And a proxy vote application with gss code "E00000002" was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000002", 6, "RECEIVED")

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 4 proxy applications (4 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 4 proxy applications (4 with EMS Elector Ids) have been pending for more than PT120H.")

        // And the message "The gss code E00000001 has 3 proxy applications (3 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000001 has 3 proxy applications \(3 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )

        // And the message "The gss code E00000002 has 1 proxy applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000002 has 1 proxy applications \(1 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )
    }

    @Test
    fun `System reports number of pending proxy downloads with an EMS Elector ID separately`() {
        // Given a proxy vote application with gss code "E00000001" with an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        createProxyApplicationWithEmsElectorId("E00000001", 6, "RECEIVED")

        // And a proxy vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 7 days ago and has record status "RECEIVED"
        setDateCreatedToDaysAgoProxy(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            7,
        )

        // And a proxy vote application with gss code "E00000001" without an EMS Elector ID was saved to EMS integration API 8 days ago and has record status "RECEIVED"
        setDateCreatedToDaysAgoProxy(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            8,
        )

        // And a proxy vote application with gss code "E00000002" without an EMS Elector ID was saved to EMS integration API 6 days ago and has record status "RECEIVED"
        setDateCreatedToDaysAgoProxy(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000002"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            6,
        )

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then the message "A total of 4 proxy applications (1 with EMS Elector Ids) have been pending for more than PT120H." is logged
        messageIsLogged("A total of 4 proxy applications (1 with EMS Elector Ids) have been pending for more than PT120H.")

        // And the message "The gss code E00000001 has 3 proxy applications (1 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000001 has 3 proxy applications \(1 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )

        // And the message "The gss code E00000002 has 1 proxy applications (0 with EMS Elector Ids) that have been pending for more than PT120H." is logged
        messageIsLoggedMatching(
            """The gss code E00000002 has 1 proxy applications \(0 with EMS Elector Ids\) that have been pending for more than PT120H\..*""",
        )
    }

    @Test
    fun `System sends an email reporting postal and proxy pending downloads`() {
        // Given the gss codes "E00000001" and "E00000002" are managed by the EROs "Camden Council" and "Westminster Council" respectively
        wireMockService.stubIerApiGetEros(
            listOf(
                buildIerEroDetails(gssCode = "E00000001", name = "Camden Council"),
                buildIerEroDetails(gssCode = "E00000002", name = "Westminster Council"),
            )
        )

        // And a postal vote application with gss code "E00000001" with an EMS Elector ID has been pending since 2025-03-03T10:00:00Z and has record status "RECEIVED"
        setDateCreatedPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
            ),
            Instant.parse("2025-03-03T10:00:00Z"),
        )

        // And a postal vote application with gss code "E00000001" without an EMS Elector ID has been pending since 2025-03-02T10:00:00Z and has record status "RECEIVED"
        setDateCreatedPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            Instant.parse("2025-03-02T10:00:00Z"),
        )

        // And a postal vote application with gss code "E00000001" without an EMS Elector ID has been pending since 2025-03-01T10:00:00Z and has record status "RECEIVED"
        setDateCreatedPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            Instant.parse("2025-03-01T10:00:00Z"),
        )

        // And a postal vote application with gss code "E00000002" without an EMS Elector ID has been pending since 2025-03-03T10:00:00Z and has record status "RECEIVED"
        setDateCreatedPostal(
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000002"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            Instant.parse("2025-03-03T10:00:00Z"),
        )

        // And a postal vote application with gss code "E00000001" was successfully downloaded by the EMS at 2025-03-05T12:00:00Z
        val deletedPostalApplication =
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf("DELETED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
            )
        setDateCreatedPostal(deletedPostalApplication, Instant.parse("2025-03-04T10:00:00Z"))
        setDateUpdatedPostal(deletedPostalApplication, "2025-03-05 12:00:00")

        // And a proxy vote application with gss code "E00000001" with an EMS Elector ID has been pending since 2025-03-03T10:00:00Z and has record status "RECEIVED"
        setDateCreatedProxy(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
            ),
            Instant.parse("2025-03-03T10:00:00Z"),
        )

        // And a proxy vote application with gss code "E00000001" without an EMS Elector ID has been pending since 2025-03-02T10:00:00Z and has record status "RECEIVED"
        setDateCreatedProxy(
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf("RECEIVED"),
                applicationDetails = buildApplicationDetailsEntity(gssCode = "E00000001"),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null),
            ),
            Instant.parse("2025-03-02T10:00:00Z"),
        )

        // When the pending downloads monitoring job runs
        pendingDownloadsMonitoringJob.monitorPendingDownloads()

        // Then an email is sent from "sender@domain.com" to "recipient1@domain.com" with subject line "Postal and Proxy Pending Downloads Monitoring" and email body...
        val expectedEmailRequest =
            buildLocalstackEmailMessage(
                emailSender = "sender@domain.com",
                toAddresses = setOf("recipient1@domain.com"),
                subject = "Postal and Proxy Pending Downloads Monitoring",
                htmlBody = emailText.trimIndent(),
                timestamp = OffsetDateTime.now(UTC).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS),
            )
        await.atMost(10, TimeUnit.SECONDS).untilAsserted {
            assertEmailSent(expectedEmailRequest)
        }
    }

    private fun createPostalApplicationWithEmsElectorId(
        gssCode: String,
        numberOfDays: Int,
        recordStatus: String,
    ) {
        val postalVoteApplication =
            buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf(recordStatus),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        setDateCreatedToDaysAgoPostal(postalVoteApplication, numberOfDays)
    }

    private fun createProxyApplicationWithEmsElectorId(
        gssCode: String,
        numberOfDays: Int,
        recordStatus: String,
    ) {
        val proxyVoteApplication =
            buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf(recordStatus),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        setDateCreatedToDaysAgoProxy(proxyVoteApplication, numberOfDays)
    }

    private fun setDateCreatedToDaysAgoPostal(
        application: PostalVoteApplication,
        daysAgo: Int,
    ) = setDateCreatedPostal(application, Instant.now().minus(Period.ofDays(daysAgo)))

    private fun setDateCreatedToDaysAgoProxy(
        application: ProxyVoteApplication,
        daysAgo: Int,
    ) = setDateCreatedProxy(application, Instant.now().minus(Period.ofDays(daysAgo)))

    private fun setDateCreatedPostal(
        application: PostalVoteApplication,
        dateCreated: Instant,
    ) {
        // Application needs to be saved first to avoid the date created being overwritten
        postalVoteApplicationRepository.save(application)
        application.dateCreated = dateCreated
        postalVoteApplicationRepository.save(application)
    }

    private fun setDateCreatedProxy(
        application: ProxyVoteApplication,
        dateCreated: Instant,
    ) {
        // Application needs to be saved first to avoid the date created being overwritten
        proxyVoteApplicationRepository.save(application)
        application.dateCreated = dateCreated
        proxyVoteApplicationRepository.save(application)
    }

    /**
     * The date_updated field is managed by the auditing entity listener so cannot be set by saving the entity.
     * We update the field with SQL to bypass this.
     */
    private fun setDateUpdatedPostal(
        application: PostalVoteApplication,
        dateUpdated: String,
    ) {
        jdbcTemplate.execute(
            "UPDATE postal_vote_application SET date_updated = '$dateUpdated' WHERE application_id = '${application.applicationId}'",
        )
    }

    private fun messageIsLogged(message: String) {
        assertThat(TestLogAppender.hasLog(message, Level.INFO)).isTrue
    }

    private fun messageIsLoggedMatching(pattern: String) {
        assertThat(TestLogAppender.getLogEventMatchingRegex(pattern, Level.INFO)).isNotNull
    }

    private fun assertEmailSent(expected: LocalstackEmailMessage) {
        with(emailMessagesSentClient.getEmailMessagesSent(localStackContainerSettings.sesMessagesUrl)) {
            val foundMessage =
                messages
                    .any {
                        it.destination.toAddresses.toSet() == expected.destination.toAddresses.toSet() &&
                            it.subject == expected.subject &&
                            deleteWhitespace(it.body.htmlPart) == deleteWhitespace(expected.body.htmlPart) &&
                            it.body.textPart == expected.body.textPart &&
                            it.source == expected.source
                    }
            assertThat(foundMessage)
                .`as` { "failed to find expectedEmailMessage[$expected], in list of messages[$messages]" }
                .isTrue
        }
    }

    private val emailText = """
      <!DOCTYPE html>
      <html lang="en">
      <head>
          <meta charset="UTF-8">
          <title>Pending EMS Integration Downloads</title>
      </head>
      <body>
          <p>A total of 4 postal applications have been pending download for more than 5 days.</p>
          <p>Of these, 1 applications have an EMS Elector ID.</p>
          <br>
          <table>
              <thead>
              <tr>
                  <th>GSS code</th>
                  <th>ERO name</th>
                  <th>Pending postal downloads</th>
                  <th>Pending with EMS Elector ID</th>
                  <th>Date of oldest pending application</th>
                  <th>Date of most recent successful EMS download</th>
              </tr>
              </thead>
              <tbody>
                  <tr>
                      <td>E00000001</td>
                      <td>Camden Council</td>
                      <td>3</td>
                      <td>1</td>
                      <td>2025-03-01T10:00:00Z</td>
                      <td>2025-03-05T12:00:00Z</td>
                  </tr>
                  <tr>
                      <td>E00000002</td>
                      <td>Westminster Council</td>
                      <td>1</td>
                      <td>0</td>
                      <td>2025-03-03T10:00:00Z</td>
                      <td>never</td>
                  </tr>
              </tbody>
          </table>
          <p>A total of 2 proxy applications have been pending download for more than 5 days.</p>
          <p>Of these, 1 applications have an EMS Elector ID.</p>
          <br>
          <table>
              <thead>
              <tr>
                  <th>GSS code</th>
                  <th>ERO name</th>
                  <th>Pending proxy downloads</th>
                  <th>Pending with EMS Elector ID</th>
                  <th>Date of oldest pending application</th>
                  <th>Date of most recent successful EMS download</th>
              </tr>
              </thead>
              <tbody>
                  <tr>
                      <td>E00000001</td>
                      <td>Camden Council</td>
                      <td>2</td>
                      <td>1</td>
                      <td>2025-03-02T10:00:00Z</td>
                      <td>never</td>
                  </tr>
              </tbody>
          </table>
      </body>
      </html>
      """
}
