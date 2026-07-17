package uk.gov.dluhc.emsintegrationapi.service

import org.apache.commons.text.StringSubstitutor.replace
import org.springframework.stereotype.Service
import uk.gov.dluhc.email.EmailClient
import uk.gov.dluhc.emsintegrationapi.config.MonitorPendingDownloadsEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.config.PendingRegisterChecksEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingDownloadSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingRegisterCheckSummary
import java.time.temporal.ChronoUnit

@Service
class EmailService(
    private val emailClient: EmailClient,
    private val pendingRegisterChecksEmailContentConfiguration: PendingRegisterChecksEmailContentConfiguration,
    private val monitorPendingDownloadsEmailContentConfiguration: MonitorPendingDownloadsEmailContentConfiguration,
) {
    fun sendRegisterCheckMonitoringEmail(
        stuckRegisterCheckSummaries: List<PendingRegisterCheckSummary>,
        totalStuck: String,
        expectedMaximumPendingPeriod: String,
    ) {
        val pendingRegisterCheckResultsHtml = generatePendingRegisterCheckResultsHtml(stuckRegisterCheckSummaries)
        val substitutionVariables = mapOf(
            "totalStuck" to totalStuck,
            "expectedMaximumPendingPeriod" to expectedMaximumPendingPeriod,
            "pendingRegisterCheckResultsHtml" to pendingRegisterCheckResultsHtml,
        )

        with(pendingRegisterChecksEmailContentConfiguration) {
            val emailToRecipients: Set<String> = recipients.split(",").map { it.trim() }.toSet()
            val emailHtmlBody = replace(emailBody, substitutionVariables)

            emailClient.send(
                emailToRecipients = emailToRecipients,
                subject = subject,
                emailHtmlBody = emailHtmlBody
            )
        }
    }

    fun sendPendingDownloadMonitoringEmail(
        postalSummary: PendingDownloadSummary,
        proxySummary: PendingDownloadSummary,
        expectedMaximumPendingPeriod: String,
    ) {
        val pendingPostalDownloadsHtml = generatePendingDownloadsHtml(postalSummary.pendingByGssCode)
        val pendingProxyDownloadsHtml = generatePendingDownloadsHtml(proxySummary.pendingByGssCode)
        val substitutionVariables = mapOf(
            "postalPending" to postalSummary.totalPending.toString(),
            "proxyPending" to proxySummary.totalPending.toString(),
            "postalPendingWithEmsElectorId" to postalSummary.totalPendingWithEmsElectorId.toString(),
            "proxyPendingWithEmsElectorId" to proxySummary.totalPendingWithEmsElectorId.toString(),
            "expectedMaximumPendingPeriod" to expectedMaximumPendingPeriod,
            "pendingPostalDownloadsHtml" to pendingPostalDownloadsHtml,
            "pendingProxyDownloadsHtml" to pendingProxyDownloadsHtml,
        )

        with(monitorPendingDownloadsEmailContentConfiguration) {
            val emailToRecipients: Set<String> = recipients.split(",").map { it.trim() }.toSet()
            val emailHtmlBody = replace(emailBody, substitutionVariables)

            emailClient.send(
                emailToRecipients = emailToRecipients,
                subject = subject,
                emailHtmlBody = emailHtmlBody
            )
        }
    }
}

private fun generatePendingRegisterCheckResultsHtml(stuckRegisterCheckSummaries: List<PendingRegisterCheckSummary>): String {
    return stuckRegisterCheckSummaries
        .sortedByDescending { it.registerCheckCount }
        .joinToString(separator = "\n") { summary ->
            """
                <tr>
                    <td>${summary.gssCode}</td>
                    <td>${summary.eroName.orEmpty()}</td>
                    <td>${summary.registerCheckCount}</td>
                    <td>${summary.earliestDateCreated?.truncatedTo(ChronoUnit.SECONDS)}</td>
                    <td>${summary.latestMatchResultSentAt?.truncatedTo(ChronoUnit.SECONDS) ?: "never"}</td>
                </tr>
            """.trimMargin()
        }
}

private fun generatePendingDownloadsHtml(pendingDownloadSummaries: List<PendingEmsDownloadSummary>): String {
    return pendingDownloadSummaries.joinToString(separator = "\n") { summary ->
        """
                <tr>
                    <td>${summary.gssCode}</td>
                    <td>${summary.eroName.orEmpty()}</td>
                    <td>${summary.pendingDownloadCount}</td>
                    <td>${summary.pendingDownloadCountWithEmsElectorId}</td>
                    <td>${summary.earliestDateCreated?.truncatedTo(ChronoUnit.SECONDS)}</td>
                    <td>${summary.lastSuccessfulEmsDownload?.truncatedTo(ChronoUnit.SECONDS) ?: "never"}</td>
                </tr>
        """.trimMargin()
    }
}
