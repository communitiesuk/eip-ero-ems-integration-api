package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.ValueMapping
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckStatus
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResult

@Mapper
interface CheckStatusMapper {

    fun toCheckStatusEntityEnum(registerCheckStatus: RegisterCheckStatus): CheckStatus

    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "PENDING")
    @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "ARCHIVED")
    fun toRegisterCheckResultEnum(checkStatus: CheckStatus): RegisterCheckResult
}
