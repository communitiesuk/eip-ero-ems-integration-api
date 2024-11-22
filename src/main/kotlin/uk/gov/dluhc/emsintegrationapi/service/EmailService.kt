package uk.gov.dluhc.emsintegrationapi.service

import org.apache.commons.text.StringSubstitutor.replace
import org.springframework.stereotype.Service
import uk.gov.dluhc.email.EmailClient
import uk.gov.dluhc.emsintegrationapi.config.PendingRegisterChecksEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckSummaryByGssCode

@Service
class EmailService(
    private val emailClient: EmailClient,
    private val pendingRegisterChecksEmailContentConfiguration: PendingRegisterChecksEmailContentConfiguration
) {
    fun sendRegisterCheckMonitoringEmail(
        stuckRegisterCheckSummaries: List<RegisterCheckSummaryByGssCode>,
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
}

fun generatePendingRegisterCheckResultsHtml(stuckRegisterCheckSummaries: List<RegisterCheckSummaryByGssCode>): String {
    return stuckRegisterCheckSummaries.joinToString(separator = "\n") { summary ->
        """
            <tr>
                <td>${summary.gssCode}</td>
                <td>${summary.registerCheckCount}</td>
            </tr>
        """.trimMargin()
    }
}
