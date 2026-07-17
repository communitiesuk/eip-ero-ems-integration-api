package uk.gov.dluhc.emsintegrationapi.service.dto

import java.time.Instant

data class PendingRegisterCheckSummary(
    val gssCode: String,
    val registerCheckCount: Int,
    val earliestDateCreated: Instant?,
    val latestMatchResultSentAt: Instant?,
    val eroName: String?,
    val emsVendor: String?,
)
