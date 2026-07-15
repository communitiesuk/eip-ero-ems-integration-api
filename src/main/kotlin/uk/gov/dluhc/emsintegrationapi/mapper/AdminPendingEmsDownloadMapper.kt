package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.dluhc.emsintegrationapi.database.entity.LastSuccessfulEmsDownloadByGssCode
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownloadSummary
import uk.gov.dluhc.emsintegrationapi.database.entity.AdminPendingEmsDownload as AdminPendingEmsDownloadEntity
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingEmsDownload as AdminPendingEmsDownloadModel

@Mapper(
    uses = [
        InstantMapper::class
    ]
)
abstract class AdminPendingEmsDownloadMapper {
    abstract fun adminPendingEmsDownloadEntityToAdminPendingEmsDownloadModel(pendingEmsDownload: AdminPendingEmsDownloadEntity): AdminPendingEmsDownloadModel

    @Mapping(target = "gssCode", source = "gssCode")
    @Mapping(target = "pendingDownloadCount", source = "pendingDownloadsSummary.pendingDownloadCount", defaultValue = "0")
    @Mapping(target = "pendingDownloadCountWithEmsElectorId", source = "pendingDownloadsSummary.pendingDownloadsWithEmsElectorId", defaultValue = "0")
    @Mapping(target = "earliestDateCreated", source = "pendingDownloadsSummary.earliestDateCreated")
    @Mapping(target = "lastSuccessfulEmsDownload", source = "lastSuccessfulDownload.lastSuccessfulEmsDownload")
    abstract fun toAdminPendingEmsDownloadSummaryModel(
        gssCode: String,
        pendingDownloadsSummary: PendingDownloadsSummaryByGssCode?,
        lastSuccessfulDownload: LastSuccessfulEmsDownloadByGssCode?,
    ): AdminPendingEmsDownloadSummary
}
