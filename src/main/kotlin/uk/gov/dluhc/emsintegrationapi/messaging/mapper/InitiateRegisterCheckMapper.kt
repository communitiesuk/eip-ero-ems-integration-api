package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.mapper.SourceTypeMapper
import uk.gov.dluhc.registercheckerapi.messaging.models.InitiateRegisterCheckForwardingMessage
import uk.gov.dluhc.registercheckerapi.messaging.models.InitiateRegisterCheckMessage
import java.util.UUID

/**
 * Maps incoming [InitiateRegisterCheckMessage] to [PendingRegisterCheckDto].
 */
@Mapper(
    uses = [SourceTypeMapper::class],
    imports = [UUID::class]
)
interface InitiateRegisterCheckMapper {

    // TODO EIP1-12676 Remove the source mapping and default expression here and replace both with an expression to assign a random UUID as part of the cleanup actions
    @Mapping(target = "correlationId", source = "correlationId", defaultExpression = "java(UUID.randomUUID())")
    @Mapping(target = "createdBy", source = "requestedBy")
    fun initiateCheckForwardingMessageToPendingRegisterCheckDto(initiateRegisterCheckMessage: InitiateRegisterCheckForwardingMessage): PendingRegisterCheckDto
}
