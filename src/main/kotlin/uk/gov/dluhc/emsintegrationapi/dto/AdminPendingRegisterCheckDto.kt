package uk.gov.dluhc.emsintegrationapi.dto

import java.time.Instant

data class AdminPendingRegisterCheckDto(
    val sourceReference: String,
    val sourceType: SourceType,
    val gssCode: String,
    val createdAt: Instant? = null,
    val historicalSearch: Boolean? = null
)
