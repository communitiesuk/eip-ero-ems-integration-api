package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.mapstruct.Mapper
import uk.gov.dluhc.emsintegrationapi.mapper.SourceTypeMapper
import uk.gov.dluhc.emsintegrationapi.messaging.dto.RegisterCheckRemovalDto
import uk.gov.dluhc.registercheckerapi.messaging.models.RemoveRegisterCheckDataMessage

@Mapper(uses = [SourceTypeMapper::class])
interface RegisterCheckRemovalMapper {

    fun toRemovalDto(message: RemoveRegisterCheckDataMessage): RegisterCheckRemovalDto
}
