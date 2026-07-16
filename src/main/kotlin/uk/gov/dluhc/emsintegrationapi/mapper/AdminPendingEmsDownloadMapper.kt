package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.Mapper
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.database.entity.AdminPendingEmsDownload as AdminPendingEmsDownloadEntity
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownload as AdminPendingEmsDownloadModel

@Mapper(
    uses = [
        InstantMapper::class
    ]
)
abstract class AdminPendingEmsDownloadMapper {
    abstract fun adminPendingEmsDownloadEntityToAdminPendingEmsDownloadModel(pendingEmsDownload: AdminPendingEmsDownloadEntity): AdminPendingEmsDownloadModel

    abstract fun pendingEmsDownloadSummaryDtoToAdminPendingEmsDownloadSummaryModel(pendingEmsDownloadSummary: PendingEmsDownloadSummary): AdminPendingEmsDownloadSummary
}
