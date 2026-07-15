package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

interface PendingDownloadsSummaryByGssCode {
    val gssCode: String
    val pendingDownloadCount: Int
    val pendingDownloadsWithEmsElectorId: Int
    val earliestDateCreated: Instant
}
