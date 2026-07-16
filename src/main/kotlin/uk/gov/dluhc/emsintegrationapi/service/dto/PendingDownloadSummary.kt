package uk.gov.dluhc.emsintegrationapi.service.dto

data class PendingDownloadSummary(
    val totalPending: Int,
    val totalPendingWithEmsElectorId: Int,
    val pendingByGssCode: List<PendingEmsDownloadSummary>
)
