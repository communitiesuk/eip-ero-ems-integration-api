package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

interface AdminPendingEmsDownload {
    val applicationId: String
    val applicationReference: String?
    val gssCode: String
    val createdAt: Instant
}
