package uk.gov.dluhc.emsintegrationapi.database.entity

interface PendingDownloadsSummaryByGssCode {
    val gssCode: String
    val pendingDownloadCount: Int
}
