package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingEmsDownloadMapper
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingChecksAndDownloadsSummaryResponse
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadsResponse
import uk.gov.dluhc.emsintegrationapi.service.AdminService
import uk.gov.dluhc.registercheckerapi.models.AdminPendingRegisterChecksResponse

private val logger = KotlinLogging.logger {}

@RestController
@CrossOrigin
class AdminController(
    private val adminService: AdminService,
    private val adminPendingRegisterCheckMapper: AdminPendingRegisterCheckMapper,
    private val adminPendingEmsDownloadMapper: AdminPendingEmsDownloadMapper
) {
    @GetMapping("/admin/pending-checks/{eroId}")
    fun adminGetPendingRegisterChecks(
        @PathVariable eroId: String
    ): AdminPendingRegisterChecksResponse {
        logger.info("Getting admin pending register checks for eroId=[$eroId]")
        return AdminPendingRegisterChecksResponse(
            pendingRegisterChecks = adminService.adminGetPendingRegisterChecks(eroId).map(
                adminPendingRegisterCheckMapper::adminPendingRegisterCheckDtoToAdminPendingRegisterCheckModel
            )
        )
    }

    @GetMapping("/admin/pending-downloads/{eroId}")
    fun adminGetPendingEmsDownloads(
        @PathVariable eroId: String
    ): AdminPendingEmsDownloadsResponse {
        logger.info("Getting admin pending EMS downloads for eroId=[$eroId]")
        return AdminPendingEmsDownloadsResponse(
            pendingEmsDownloads = adminService.adminGetPendingEmsDownloads(eroId).map(
                adminPendingEmsDownloadMapper::adminPendingEmsDownloadEntityToAdminPendingEmsDownloadModel
            )
        )
    }

    @GetMapping("/admin/pending-checks-and-downloads-summary")
    fun adminGetPendingChecksAndDownloadsSummary(
        @RequestParam(defaultValue = "1") registerChecksPendingMinAgeInDays: Int,
        @RequestParam(defaultValue = "5") emsDownloadsPendingMinAgeInDays: Int,
    ): AdminPendingChecksAndDownloadsSummaryResponse {
        logger.info(
            "Getting admin pending checks and downloads summary with minimum pending ages " +
                "[registerChecks=$registerChecksPendingMinAgeInDays days, emsDownloads=$emsDownloadsPendingMinAgeInDays days]"
        )

        return AdminPendingChecksAndDownloadsSummaryResponse(
            pendingRegisterChecks = adminService.adminGetPendingRegisterChecksSummary(registerChecksPendingMinAgeInDays),
            pendingPostalDownloads = adminService.adminGetPendingPostalDownloadsSummary(emsDownloadsPendingMinAgeInDays),
            pendingProxyDownloads = adminService.adminGetPendingProxyDownloadsSummary(emsDownloadsPendingMinAgeInDays),
        )
    }
}
