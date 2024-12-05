package uk.gov.dluhc.emsintegrationapi.service.dto

import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode

data class PendingDownloadSummary(
    val totalPending: Int,
    val totalPendingWithEmsElectorId: Int,
    val pendingByGssCode: List<PendingDownloadsSummaryByGssCode>
)
