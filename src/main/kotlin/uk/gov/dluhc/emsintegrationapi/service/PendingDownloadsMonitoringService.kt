package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingDownloadSummary
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class PendingDownloadsMonitoringService(
    private val postalApplicationsRepository: PostalVoteApplicationRepository,
    private val proxyApplicationsRepository: ProxyVoteApplicationRepository,
    private val emailService: EmailService,
    @Value("\${jobs.pending-downloads-monitoring.expected-maximum-pending-period}") private val expectedMaximumPendingPeriod: Duration,
    @Value("\${jobs.pending-downloads-monitoring.excluded-gss-codes}") private val excludedGssCodes: List<String>,
    @Value("\${jobs.pending-downloads-monitoring.send-email}") private val sendEmail: Boolean,
) {

    @Transactional(readOnly = true)
    fun monitorPendingDownloads() {
        val createdBefore = Instant.now().minus(expectedMaximumPendingPeriod)
        val expectedMaximumPendingDays = expectedMaximumPendingPeriod.toDays()

        val pendingPostalDownloads = filterAndSummarisePendingDownloads(
            postalApplicationsRepository.summarisePendingPostalVotesByGssCode(createdBefore)
        )
        val pendingProxyDownloads = filterAndSummarisePendingDownloads(
            proxyApplicationsRepository.summarisePendingProxyVotesByGssCode(createdBefore)
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

    private fun filterAndSummarisePendingDownloads(summaries: List<PendingDownloadsSummaryByGssCode>): PendingDownloadSummary {
        val nonExcludedPendingDownloadSummaries = summaries.filter { !excludedGssCodes.contains(it.gssCode) }
        val totalPending = nonExcludedPendingDownloadSummaries.sumOf { it.pendingDownloadCount }
        val totalPendingWithEmsElectorId =
            nonExcludedPendingDownloadSummaries.sumOf { it.pendingDownloadsWithEmsElectorId }
        return PendingDownloadSummary(
            totalPending = totalPending,
            totalPendingWithEmsElectorId = totalPendingWithEmsElectorId,
            pendingByGssCode = nonExcludedPendingDownloadSummaries,
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
                        "(${it.pendingDownloadsWithEmsElectorId} with EMS Elector Ids) " +
                        "that have been pending for more than $expectedMaximumPendingPeriod."
                }
            }
        }
    }
}
