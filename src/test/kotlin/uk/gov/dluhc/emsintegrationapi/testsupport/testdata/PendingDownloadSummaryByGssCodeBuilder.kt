package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode

data class PendingDownloadsSummaryByGssCodeImpl(
    override val gssCode: String,
    override val pendingDownloadCount: Int,
    override val pendingDownloadsWithEmsElectorId: Int,
) : PendingDownloadsSummaryByGssCode

fun buildPendingDownloadsSummaryByGssCode(
    gssCode: String = "E09000021",
    pendingDownloadCount: Int,
    pendingDownloadsWithEmsElectorId: Int
): PendingDownloadsSummaryByGssCode = PendingDownloadsSummaryByGssCodeImpl(
    gssCode = gssCode,
    pendingDownloadCount = pendingDownloadCount,
    pendingDownloadsWithEmsElectorId = pendingDownloadsWithEmsElectorId
)
