package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingDownloadSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class PendingDownloadsMonitoringService(
    private val pendingEmsDownloadSummaryService: PendingEmsDownloadSummaryService,
    private val emailService: EmailService,
    @Value("\${jobs.pending-downloads-monitoring.expected-maximum-pending-period}") private val expectedMaximumPendingPeriod: Duration,
    @Value("\${jobs.pending-downloads-monitoring.excluded-gss-codes}") private val excludedGssCodes: List<String>,
    @Value("\${jobs.pending-downloads-monitoring.send-email}") private val sendEmail: Boolean,
) {

    @Transactional(readOnly = true)
    fun monitorPendingDownloads() {
        val createdBefore = Instant.now().minus(expectedMaximumPendingPeriod)
        val expectedMaximumPendingDays = expectedMaximumPendingPeriod.toDays()

        val pendingPostalDownloads = summariseTotals(
            pendingEmsDownloadSummaryService.summarisePendingPostalDownloads(createdBefore, excludedGssCodes)
        )
        val pendingProxyDownloads = summariseTotals(
            pendingEmsDownloadSummaryService.summarisePendingProxyDownloads(createdBefore, excludedGssCodes)
        )

        logPendingDownloads("postal", pendingPostalDownloads)
        logPendingDownloads("proxy", pendingProxyDownloads)

        if (sendEmail) {
            emailService.sendPendingDownloadMonitoringEmail(
                postalSummary = pendingPostalDownloads,
                proxySummary = pendingProxyDownloads,
                expectedMaximumPendingPeriod = "$expectedMaximumPendingDays days",
            )
        }
    }

    private fun summariseTotals(summaries: List<PendingEmsDownloadSummary>): PendingDownloadSummary {
        val pendingDownloadSummaries = summaries.filter { it.pendingDownloadCount > 0 }
        val totalPending = pendingDownloadSummaries.sumOf { it.pendingDownloadCount }
        val totalPendingWithEmsElectorId = pendingDownloadSummaries.sumOf { it.pendingDownloadCountWithEmsElectorId }
        return PendingDownloadSummary(
            totalPending = totalPending,
            totalPendingWithEmsElectorId = totalPendingWithEmsElectorId,
            pendingByGssCode = pendingDownloadSummaries,
        )
    }

    private fun logPendingDownloads(applicationType: String, summary: PendingDownloadSummary) {
        with(summary) {
            logger.info {
                "A total of $totalPending $applicationType applications ($totalPendingWithEmsElectorId with EMS " +
                    "Elector Ids) have been pending for more than $expectedMaximumPendingPeriod."
            }
            pendingByGssCode.forEach {
                logger.info {
                    "The gss code ${it.gssCode} has ${it.pendingDownloadCount} $applicationType applications " +
                        "(${it.pendingDownloadCountWithEmsElectorId} with EMS Elector Ids) " +
                        "that have been pending for more than $expectedMaximumPendingPeriod. " +
                        "The oldest pending application has been pending since ${it.earliestDateCreated}. " +
                        "The last successful EMS download was at ${it.lastSuccessfulEmsDownload ?: "never"}." +
                        (it.eroName?.let { eroName -> " The ERO name is $eroName." } ?: "")
                }
            }
        }
    }
}
