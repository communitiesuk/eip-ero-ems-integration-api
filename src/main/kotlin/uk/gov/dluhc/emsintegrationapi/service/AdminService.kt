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
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingChecksAndDownloadsSummaryResponse
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AdminService(
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val registerCheckRepository: RegisterCheckRepository,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val adminPendingRegisterCheckMapper: AdminPendingRegisterCheckMapper,
    private val adminPendingEmsDownloadMapper: AdminPendingEmsDownloadMapper,
    private val pendingRegisterCheckSummaryService: PendingRegisterCheckSummaryService,
    private val pendingEmsDownloadSummaryService: PendingEmsDownloadSummaryService,
    private val retrieveEroNameService: RetrieveEroDetailsService,
    @Value("\${admin.pending-checks-and-downloads-summary.excluded-register-check-gss-codes}") private val registerCheckExcludedGssCodes: List<String>,
    @Value("\${admin.pending-checks-and-downloads-summary.excluded-ems-download-gss-codes}") private val pendingDownloadsExcludedGssCodes: List<String>,
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
    fun adminGetPendingChecksAndDownloadsSummary(
        registerChecksPendingMinAgeInDays: Int,
        emsDownloadsPendingMinAgeInDays: Int,
    ): AdminPendingChecksAndDownloadsSummaryResponse {
        val now = Instant.now()
        val registerChecksCreatedBefore = now.minus(registerChecksPendingMinAgeInDays.toLong(), ChronoUnit.DAYS)
        val emsDownloadsCreatedBefore = now.minus(emsDownloadsPendingMinAgeInDays.toLong(), ChronoUnit.DAYS)
        val eroSummaryByGssCode = retrieveEroNameService.getEroSummaryByGssCode()
        return AdminPendingChecksAndDownloadsSummaryResponse(
            pendingRegisterChecks = pendingRegisterCheckSummaryService
                .summarisePendingRegisterChecks(registerChecksCreatedBefore, registerCheckExcludedGssCodes, eroSummaryByGssCode)
                .map(adminPendingRegisterCheckMapper::pendingRegisterCheckSummaryDtoToAdminPendingRegisterCheckSummaryModel),
            pendingPostalDownloads = pendingEmsDownloadSummaryService
                .summarisePendingPostalDownloads(emsDownloadsCreatedBefore, pendingDownloadsExcludedGssCodes, eroSummaryByGssCode)
                .map(adminPendingEmsDownloadMapper::pendingEmsDownloadSummaryDtoToAdminPendingEmsDownloadSummaryModel),
            pendingProxyDownloads = pendingEmsDownloadSummaryService
                .summarisePendingProxyDownloads(emsDownloadsCreatedBefore, pendingDownloadsExcludedGssCodes, eroSummaryByGssCode)
                .map(adminPendingEmsDownloadMapper::pendingEmsDownloadSummaryDtoToAdminPendingEmsDownloadSummaryModel),
        )
    }
}
