package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.LastSuccessfulEmsDownloadByGssCode
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.service.dto.EroSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import java.time.Instant

private val logger = KotlinLogging.logger {}
/**
 * Summarises pending EMS downloads per GSS code, combining the pending download counts with the
 * last successful EMS download for each GSS code. Used by both the admin summary endpoint and
 * the pending downloads monitoring job.
 */
@Service
class PendingEmsDownloadSummaryService(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val retrieveEroNameService: RetrieveEroDetailsService,
) {
    @Transactional(readOnly = true)
    fun summarisePendingPostalDownloads(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingEmsDownloadSummary> =
        summarisePendingPostalDownloads(createdBefore, excludedGssCodes, retrieveEroNameService.getEroSummaryByGssCode())

    @Transactional(readOnly = true)
    fun summarisePendingPostalDownloads(
        createdBefore: Instant,
        excludedGssCodes: List<String>,
        eroSummaryByGssCode: Map<String, EroSummary>,
    ): List<PendingEmsDownloadSummary> {
        logger.info { "Summarising pending postal downloads" }
        val downloads = summarisePendingDownloads(
            postalVoteApplicationRepository.summarisePendingPostalVotesByGssCode(createdBefore),
            postalVoteApplicationRepository.getLastSuccessfulEmsDownloadByGssCode(),
            excludedGssCodes,
            eroSummaryByGssCode,
        )
        logger.info { "postal downloads complete" }
        return downloads
    }

    @Transactional(readOnly = true)
    fun summarisePendingProxyDownloads(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingEmsDownloadSummary> =
        summarisePendingProxyDownloads(createdBefore, excludedGssCodes, retrieveEroNameService.getEroSummaryByGssCode())

    @Transactional(readOnly = true)
    fun summarisePendingProxyDownloads(
        createdBefore: Instant,
        excludedGssCodes: List<String>,
        eroSummaryByGssCode: Map<String, EroSummary>,
    ): List<PendingEmsDownloadSummary>
    {
        logger.info { "Summarising pending proxy downloads" }
        val downloads = summarisePendingDownloads(
            proxyVoteApplicationRepository.summarisePendingProxyVotesByGssCode(createdBefore),
            proxyVoteApplicationRepository.getLastSuccessfulEmsDownloadByGssCode(),
            excludedGssCodes,
            eroSummaryByGssCode,
        )
        logger.info { "Proxy downloads completed" }
        return downloads
    }

    private fun summarisePendingDownloads(
        pendingSummaries: List<PendingDownloadsSummaryByGssCode>,
        lastSuccessfulDownloads: List<LastSuccessfulEmsDownloadByGssCode>,
        excludedGssCodes: List<String>,
        eroSummaryByGssCode: Map<String, EroSummary>,
    ): List<PendingEmsDownloadSummary> {
        val lastSuccessfulDownloadsByGssCode = lastSuccessfulDownloads.associateBy { it.gssCode }
        return pendingSummaries
            .filter { it.gssCode !in excludedGssCodes }
            .sortedWith(
                compareByDescending<PendingDownloadsSummaryByGssCode> { it.pendingDownloadCount }
                    .thenBy { it.gssCode }
            )
            .map { pendingSummary ->
                val eroSummary = eroSummaryByGssCode[pendingSummary.gssCode]
                PendingEmsDownloadSummary(
                    gssCode = pendingSummary.gssCode,
                    pendingDownloadCount = pendingSummary.pendingDownloadCount,
                    pendingDownloadCountWithEmsElectorId = pendingSummary.pendingDownloadsWithEmsElectorId,
                    earliestDateCreated = pendingSummary.earliestDateCreated,
                    lastSuccessfulEmsDownload = lastSuccessfulDownloadsByGssCode[pendingSummary.gssCode]?.lastSuccessfulEmsDownload,
                    eroName = eroSummary?.name,
                    eroId = eroSummary?.eroId,
                    emsVendor = eroSummary?.emsVendor,
                )
            }
    }
}
