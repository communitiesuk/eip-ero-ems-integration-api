package uk.gov.dluhc.emsintegrationapi.messaging.dto

import uk.gov.dluhc.emsintegrationapi.dto.SourceType

data class RegisterCheckRemovalDto(
    val sourceType: SourceType,
    val sourceReference: String,
)
