package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class PendingDownloadsMonitoringService(
    private val postalApplicationsRepository: PostalVoteApplicationRepository,
    private val proxyApplicationsRepository: ProxyVoteApplicationRepository,
    @Value("\${jobs.pending-downloads-monitoring.expected-maximum-pending-period}") private val expectedMaximumPendingPeriod: Duration,
    @Value("\${jobs.pending-downloads-monitoring.excluded-gss-codes}") private val excludedGssCodes: List<String>,
) {

    @Transactional(readOnly = true)
    fun monitorPendingDownloads() {
        val createdBefore = Instant.now().minus(expectedMaximumPendingPeriod)

        postalApplicationsRepository.summarisePendingPostalVotesByGssCode(createdBefore).apply {
            logPendingDownloads("postal", this)
        }

        proxyApplicationsRepository.summarisePendingProxyVotesByGssCode(createdBefore).apply {
            logPendingDownloads("proxy", this)
        }
    }

    private fun logPendingDownloads(applicationType: String, summaries: List<PendingDownloadsSummaryByGssCode>) {
        val nonExcludedPendingDownloadSummaries = summaries.filter { !excludedGssCodes.contains(it.gssCode) }
        val totalPending = nonExcludedPendingDownloadSummaries.sumOf { it.pendingDownloadCount }

        logger.info { "A total of $totalPending $applicationType applications have been pending for more than $expectedMaximumPendingPeriod." }
        nonExcludedPendingDownloadSummaries.forEach {
            logger.info {
                "The gss code ${it.gssCode} has ${it.pendingDownloadCount} $applicationType applications " +
                    "that have been pending for more than $expectedMaximumPendingPeriod."
            }
        }
    }
}
