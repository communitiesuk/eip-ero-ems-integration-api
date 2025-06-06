package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.dto.AdminPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.dto.SourceType
import java.time.Instant
import java.util.UUID

fun buildAdminPendingRegisterCheckDto(
    sourceReference: String = UUID.randomUUID().toString(),
    sourceType: SourceType = SourceType.VOTER_CARD,
    gssCode: String = "E09000021",
    createdAt: Instant? = null,
    historicalSearch: Boolean? = null,
) = AdminPendingRegisterCheckDto(
    sourceReference = sourceReference,
    sourceType = sourceType,
    gssCode = gssCode,
    createdAt = createdAt,
    historicalSearch = historicalSearch,
)
