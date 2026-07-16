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
    fun adminGetPendingRegisterChecksSummary(pendingMinAgeInDays: Int): List<AdminPendingRegisterCheckSummary> {
        val createdBefore = Instant.now().minus(pendingMinAgeInDays.toLong(), ChronoUnit.DAYS)
        return pendingRegisterCheckSummaryService
            .summarisePendingRegisterChecks(createdBefore, registerCheckExcludedGssCodes)
            .map(adminPendingRegisterCheckMapper::pendingRegisterCheckSummaryDtoToAdminPendingRegisterCheckSummaryModel)
    }

    @Transactional(readOnly = true)
    fun adminGetPendingPostalDownloadsSummary(pendingMinAgeInDays: Int): List<AdminPendingEmsDownloadSummary> {
        val createdBefore = Instant.now().minus(pendingMinAgeInDays.toLong(), ChronoUnit.DAYS)
        return pendingEmsDownloadSummaryService
            .summarisePendingPostalDownloads(createdBefore, pendingDownloadsExcludedGssCodes)
            .map(adminPendingEmsDownloadMapper::pendingEmsDownloadSummaryDtoToAdminPendingEmsDownloadSummaryModel)
    }

    @Transactional(readOnly = true)
    fun adminGetPendingProxyDownloadsSummary(pendingMinAgeInDays: Int): List<AdminPendingEmsDownloadSummary> {
        val createdBefore = Instant.now().minus(pendingMinAgeInDays.toLong(), ChronoUnit.DAYS)
        return pendingEmsDownloadSummaryService
            .summarisePendingProxyDownloads(createdBefore, pendingDownloadsExcludedGssCodes)
            .map(adminPendingEmsDownloadMapper::pendingEmsDownloadSummaryDtoToAdminPendingEmsDownloadSummaryModel)
    }
}
