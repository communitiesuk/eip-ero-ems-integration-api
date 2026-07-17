package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
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
    private val retrieveEroNameService: RetrieveEroNameService,
) {
    @Transactional(readOnly = true)
    fun summarisePendingRegisterChecks(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingRegisterCheckSummary> {
        val mostRecentResponsesByGssCode = registerCheckRepository.findMostRecentResponseTimeForEachGssCode()
            .associateBy { it.gssCode }
        val eroNamesByGssCode = retrieveEroNameService.getEroNamesByGssCode()

        return registerCheckRepository.summarisePendingRegisterChecksByGssCode(createdBefore)
            .filter { !excludedGssCodes.contains(it.gssCode) }
            .sortedBy { it.gssCode }
            .map { pendingSummary ->
                PendingRegisterCheckSummary(
                    gssCode = pendingSummary.gssCode,
                    registerCheckCount = pendingSummary.registerCheckCount,
                    earliestDateCreated = pendingSummary.earliestDateCreated,
                    latestMatchResultSentAt = mostRecentResponsesByGssCode[pendingSummary.gssCode]?.latestMatchResultSentAt,
                    eroName = eroNamesByGssCode[pendingSummary.gssCode],
                )
            }
    }
}
