package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.dto

import uk.gov.dluhc.emsintegrationapi.messaging.dto.RegisterCheckRemovalDto
import java.util.UUID.randomUUID

fun buildRegisterCheckRemovalDto(
    sourceReference: String = randomUUID().toString(),
) = RegisterCheckRemovalDto(
    sourceReference = sourceReference,
)
