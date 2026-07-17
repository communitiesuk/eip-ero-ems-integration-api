package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.service.dto.EroSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingRegisterCheckSummary
import java.time.Instant

/**
 * Summarizes pending register checks per GSS code, combining the pending check counts with the
 * most recent EMS response time for each GSS code. Used by both the admin summary endpoint and
 * the register check monitoring job.
 */
@Service
class PendingRegisterCheckSummaryService(
    private val registerCheckRepository: RegisterCheckRepository,
    private val retrieveEroNameService: RetrieveEroDetailsService,
) {
    @Transactional(readOnly = true)
    fun summarisePendingRegisterChecks(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingRegisterCheckSummary> =
        summarisePendingRegisterChecks(createdBefore, excludedGssCodes, retrieveEroNameService.getEroSummaryByGssCode())

    @Transactional(readOnly = true)
    fun summarisePendingRegisterChecks(
        createdBefore: Instant,
        excludedGssCodes: List<String>,
        eroSummaryByGssCode: Map<String, EroSummary>,
    ): List<PendingRegisterCheckSummary> {
        val mostRecentResponsesByGssCode = registerCheckRepository.findMostRecentResponseTimeForEachGssCode()
            .associateBy { it.gssCode }
        return registerCheckRepository.summarisePendingRegisterChecksByGssCode(createdBefore)
            .filter { it.gssCode !in excludedGssCodes }
            .sortedBy { it.gssCode }
            .map { pendingSummary ->
                val eroSummary = eroSummaryByGssCode[pendingSummary.gssCode]
                PendingRegisterCheckSummary(
                    gssCode = pendingSummary.gssCode,
                    registerCheckCount = pendingSummary.registerCheckCount,
                    earliestDateCreated = pendingSummary.earliestDateCreated,
                    latestMatchResultSentAt = mostRecentResponsesByGssCode[pendingSummary.gssCode]?.latestMatchResultSentAt,
                    eroName = eroSummary?.name,
                    emsVendor = eroSummary?.emsVendor,
                )
            }
    }
}
