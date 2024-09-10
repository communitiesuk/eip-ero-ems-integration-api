package uk.gov.dluhc.emsintegrationapi.service

import liquibase.repackaged.org.apache.commons.text.StringSubstitutor.replace
import org.springframework.stereotype.Service
import uk.gov.dluhc.email.EmailClient
import uk.gov.dluhc.emsintegrationapi.config.MonitorPendingDownloadsEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingDownloadSummary

@Service
class EmailService(
    private val emailClient: EmailClient,
    private val monitorPendingDownloadsEmailContentConfiguration: MonitorPendingDownloadsEmailContentConfiguration
) {
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

    private fun generatePendingDownloadsHtml(pendingDownloadSummaries: List<PendingDownloadsSummaryByGssCode>): String {
        return pendingDownloadSummaries.joinToString(separator = "\n") { summary ->
            """
                <tr>
                    <td>${summary.gssCode}</td>
                    <td>${summary.pendingDownloadCount}</td>
                    <td>${summary.pendingDownloadsWithEmsElectorId}</td>
                </tr>
            """.trimMargin()
        }
    }
}
