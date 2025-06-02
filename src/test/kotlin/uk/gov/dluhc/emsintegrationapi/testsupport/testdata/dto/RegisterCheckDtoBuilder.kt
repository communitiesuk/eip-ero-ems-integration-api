package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.dto.PersonalDetailDto
import uk.gov.dluhc.emsintegrationapi.dto.SourceType
import java.time.Instant
import java.util.UUID

fun buildPendingRegisterCheckDto(
    correlationId: UUID = UUID.randomUUID(),
    sourceReference: String = UUID.randomUUID().toString(),
    sourceCorrelationId: UUID = UUID.randomUUID(),
    sourceType: SourceType = SourceType.VOTER_CARD,
    gssCode: String = "E09000021",
    personalDetail: PersonalDetailDto = buildPersonalDetailDto(),
    createdBy: String = "system",
    createdAt: Instant? = null
) = PendingRegisterCheckDto(
    correlationId = correlationId,
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    sourceType = sourceType,
    gssCode = gssCode,
    personalDetail = personalDetail,
    createdBy = createdBy,
    createdAt = createdAt
)
