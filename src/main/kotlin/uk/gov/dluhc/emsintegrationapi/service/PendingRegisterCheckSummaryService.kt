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
) {
    @Transactional(readOnly = true)
    fun summarisePendingRegisterChecks(createdBefore: Instant, excludedGssCodes: List<String>): List<PendingRegisterCheckSummary> {
        val pendingSummariesByGssCode = registerCheckRepository.summarisePendingRegisterChecksByGssCode(createdBefore)
            .filter { !excludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }
        val mostRecentResponsesByGssCode = registerCheckRepository.findMostRecentResponseTimeForEachGssCode()
            .filter { !excludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }

        return (pendingSummariesByGssCode.keys + mostRecentResponsesByGssCode.keys)
            .sorted()
            .map { gssCode ->
                val pendingSummary = pendingSummariesByGssCode[gssCode]
                PendingRegisterCheckSummary(
                    gssCode = gssCode,
                    registerCheckCount = pendingSummary?.registerCheckCount ?: 0,
                    earliestDateCreated = pendingSummary?.earliestDateCreated,
                    latestMatchResultSentAt = mostRecentResponsesByGssCode[gssCode]?.latestMatchResultSentAt,
                )
            }
    }
}
