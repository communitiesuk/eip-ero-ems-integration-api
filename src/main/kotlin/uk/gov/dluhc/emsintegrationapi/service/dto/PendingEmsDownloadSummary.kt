package uk.gov.dluhc.emsintegrationapi.service.dto

import java.time.Instant

data class PendingEmsDownloadSummary(
    val gssCode: String,
    val pendingDownloadCount: Int,
    val pendingDownloadCountWithEmsElectorId: Int,
    val earliestDateCreated: Instant?,
    val lastSuccessfulEmsDownload: Instant?,
)
