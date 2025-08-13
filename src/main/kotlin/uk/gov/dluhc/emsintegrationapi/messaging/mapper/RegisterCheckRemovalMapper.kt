package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.mapstruct.Mapper
import uk.gov.dluhc.emsintegrationapi.messaging.dto.RegisterCheckRemovalDto
import uk.gov.dluhc.registercheckerapi.messaging.models.RemoveRegisterCheckDataMessage

@Mapper
interface RegisterCheckRemovalMapper {

    fun toRemovalDto(message: RemoveRegisterCheckDataMessage): RegisterCheckRemovalDto
}
