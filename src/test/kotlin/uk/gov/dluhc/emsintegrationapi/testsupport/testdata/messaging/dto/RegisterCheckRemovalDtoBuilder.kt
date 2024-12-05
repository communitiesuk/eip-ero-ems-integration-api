package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.dto

import uk.gov.dluhc.emsintegrationapi.dto.SourceType
import uk.gov.dluhc.emsintegrationapi.messaging.dto.RegisterCheckRemovalDto
import java.util.UUID.randomUUID

fun buildRegisterCheckRemovalDto(
    sourceType: SourceType = SourceType.VOTER_CARD,
    sourceReference: String = randomUUID().toString(),
) = RegisterCheckRemovalDto(
    sourceType = sourceType,
    sourceReference = sourceReference,
)
