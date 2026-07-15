package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.AdminPendingEmsDownload
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.dto.AdminPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingEmsDownloadMapper
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingRegisterCheckSummary
import java.time.Instant

@Service
class AdminService(
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val registerCheckRepository: RegisterCheckRepository,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val adminPendingRegisterCheckMapper: AdminPendingRegisterCheckMapper,
    private val adminPendingEmsDownloadMapper: AdminPendingEmsDownloadMapper,
    @Value("\${jobs.register-check-monitoring.excluded-gss-codes}") private val registerCheckExcludedGssCodes: List<String>,
    @Value("\${jobs.pending-downloads-monitoring.excluded-gss-codes}") private val pendingDownloadsExcludedGssCodes: List<String>,
) {
    companion object {
        private const val MAX_RESULTS = 10000
    }

    @Transactional(readOnly = true)
    fun adminGetPendingRegisterChecks(eroId: String): List<AdminPendingRegisterCheckDto> {
        val gssCodes = retrieveGssCodeService.getGssCodesFromEroId(eroId)
        return registerCheckRepository.adminFindPendingEntriesByGssCodes(gssCodes, MAX_RESULTS)
            .map(adminPendingRegisterCheckMapper::registerCheckEntityToAdminPendingRegisterCheckDto)
    }

    @Transactional(readOnly = true)
    fun adminGetPendingEmsDownloads(eroId: String): List<AdminPendingEmsDownload> {
        val gssCodes = retrieveGssCodeService.getGssCodesFromEroId(eroId)
        val pendingPostalDownloads = postalVoteApplicationRepository.adminFindPendingPostalVoteDownloadsByGssCodes(gssCodes, MAX_RESULTS)
        val pendingProxyDownloads = proxyVoteApplicationRepository.adminFindPendingProxyVoteDownloadsByGssCodes(gssCodes, MAX_RESULTS)

        return pendingPostalDownloads.plus(pendingProxyDownloads).sortedBy { it.createdAt }.take(MAX_RESULTS)
    }

    @Transactional(readOnly = true)
    fun adminGetPendingRegisterChecksSummary(): List<AdminPendingRegisterCheckSummary> {
        val pendingSummariesByGssCode = registerCheckRepository.summarisePendingRegisterChecksByGssCode(Instant.now())
            .filter { !registerCheckExcludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }
        val mostRecentResponsesByGssCode = registerCheckRepository.findMostRecentResponseTimeForEachGssCode()
            .filter { !registerCheckExcludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }

        return (pendingSummariesByGssCode.keys + mostRecentResponsesByGssCode.keys)
            .sorted()
            .map { gssCode ->
                adminPendingRegisterCheckMapper.toAdminPendingRegisterCheckSummaryModel(
                    gssCode = gssCode,
                    pendingChecksSummary = pendingSummariesByGssCode[gssCode],
                    mostRecentResponse = mostRecentResponsesByGssCode[gssCode],
                )
            }
    }

    @Transactional(readOnly = true)
    fun adminGetPendingEmsDownloadsSummary(): List<AdminPendingEmsDownloadSummary> {
        val pendingSummariesByGssCode = postalVoteApplicationRepository.summarisePendingPostalVotesByGssCode(Instant.now())
            .filter { !pendingDownloadsExcludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }
        val lastSuccessfulDownloadsByGssCode = postalVoteApplicationRepository.getLastSuccessfulEmsDownloadByGssCode()
            .filter { !pendingDownloadsExcludedGssCodes.contains(it.gssCode) }
            .associateBy { it.gssCode }

        return (pendingSummariesByGssCode.keys + lastSuccessfulDownloadsByGssCode.keys)
            .sorted()
            .map { gssCode ->
                adminPendingEmsDownloadMapper.toAdminPendingEmsDownloadSummaryModel(
                    gssCode = gssCode,
                    pendingDownloadsSummary = pendingSummariesByGssCode[gssCode],
                    lastSuccessfulDownload = lastSuccessfulDownloadsByGssCode[gssCode],
                )
            }
    }
}
