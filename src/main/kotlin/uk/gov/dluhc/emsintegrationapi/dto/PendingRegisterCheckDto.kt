package uk.gov.dluhc.emsintegrationapi.dto

import java.time.Instant
import java.util.UUID

data class PendingRegisterCheckDto(
    val correlationId: UUID,
    val sourceReference: String,
    val sourceCorrelationId: UUID,
    val gssCode: String,
    val personalDetail: PersonalDetailDto,
    val emsElectorId: String? = null,
    val historicalSearch: Boolean? = null,
    val createdBy: String,
    val createdAt: Instant? = null
)
