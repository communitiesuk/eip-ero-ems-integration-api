package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.LastSuccessfulEmsDownloadByGssCode
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import java.time.Instant

/**
 * Summarises pending EMS downloads per GSS code, combining the pending download counts with the
 * last successful EMS download for each GSS code. Used by both the admin summary endpoint and
 * the pending downloads monitoring job.
 */
@Service
class PendingEmsDownloadSummaryService(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
) {
    @Transactional(readOnly = true)
    fun summarisePendingPostalDownloads(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingEmsDownloadSummary> =
        summarisePendingDownloads(
            postalVoteApplicationRepository.summarisePendingPostalVotesByGssCode(createdBefore),
            postalVoteApplicationRepository.getLastSuccessfulEmsDownloadByGssCode(),
            excludedGssCodes,
        )

    @Transactional(readOnly = true)
    fun summarisePendingProxyDownloads(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingEmsDownloadSummary> =
        summarisePendingDownloads(
            proxyVoteApplicationRepository.summarisePendingProxyVotesByGssCode(createdBefore),
            proxyVoteApplicationRepository.getLastSuccessfulEmsDownloadByGssCode(),
            excludedGssCodes,
        )

    private fun summarisePendingDownloads(
        pendingSummaries: List<PendingDownloadsSummaryByGssCode>,
        lastSuccessfulDownloads: List<LastSuccessfulEmsDownloadByGssCode>,
        excludedGssCodes: List<String>,
    ): List<PendingEmsDownloadSummary> {
        val pendingSummariesByGssCode = pendingSummaries
            .filter { !excludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }
        val lastSuccessfulDownloadsByGssCode = lastSuccessfulDownloads
            .filter { !excludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }

        return (pendingSummariesByGssCode.keys + lastSuccessfulDownloadsByGssCode.keys)
            .sorted()
            .map { gssCode ->
                val pendingSummary = pendingSummariesByGssCode[gssCode]
                PendingEmsDownloadSummary(
                    gssCode = gssCode,
                    pendingDownloadCount = pendingSummary?.pendingDownloadCount ?: 0,
                    pendingDownloadCountWithEmsElectorId = pendingSummary?.pendingDownloadsWithEmsElectorId ?: 0,
                    earliestDateCreated = pendingSummary?.earliestDateCreated,
                    lastSuccessfulEmsDownload = lastSuccessfulDownloadsByGssCode[gssCode]?.lastSuccessfulEmsDownload,
                )
            }
    }
}
