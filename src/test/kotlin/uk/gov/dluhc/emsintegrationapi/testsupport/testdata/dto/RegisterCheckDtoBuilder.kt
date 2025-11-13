package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.dto.PersonalDetailDto
import java.time.Instant
import java.util.UUID

fun buildPendingRegisterCheckDto(
    correlationId: UUID = UUID.randomUUID(),
    sourceReference: String = UUID.randomUUID().toString(),
    applicationReference: String? = "P123412345",
    sourceCorrelationId: UUID = UUID.randomUUID(),
    gssCode: String = "E09000021",
    personalDetail: PersonalDetailDto = buildPersonalDetailDto(),
    createdBy: String = "system",
    createdAt: Instant? = null
) = PendingRegisterCheckDto(
    correlationId = correlationId,
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    applicationReference = applicationReference,
    gssCode = gssCode,
    personalDetail = personalDetail,
    createdBy = createdBy,
    createdAt = createdAt
)
