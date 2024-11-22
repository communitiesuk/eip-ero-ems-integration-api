package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.InheritInverseConfiguration
import org.mapstruct.Mapper
import org.mapstruct.ValueMapping
import uk.gov.dluhc.emsintegrationapi.dto.SourceType
import uk.gov.dluhc.registercheckerapi.models.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceType as SourceTypeEntityEnum
import uk.gov.dluhc.emsintegrationapi.dto.SourceType as SourceTypeDtoEnum
import uk.gov.dluhc.registercheckerapi.messaging.models.SourceType as SourceTypeSqsEnum

@Mapper
interface SourceTypeMapper {

    @ValueMapping(target = "VOTER_CARD", source = "VOTER_MINUS_CARD")
    @ValueMapping(target = "POSTAL_VOTE", source = "POSTAL_MINUS_VOTE")
    @ValueMapping(target = "PROXY_VOTE", source = "PROXY_MINUS_VOTE")
    @ValueMapping(target = "OVERSEAS_VOTE", source = "OVERSEAS_MINUS_VOTE")
    @ValueMapping(target = "APPLICATIONS_API", source = "APPLICATIONS_MINUS_API")
    fun fromSqsToDtoEnum(sqsSourceType: SourceTypeSqsEnum): SourceTypeDtoEnum

    @ValueMapping(target = "VOTER_MINUS_CARD", source = "VOTER_CARD")
    @ValueMapping(target = "POSTAL_MINUS_VOTE", source = "POSTAL_VOTE")
    @ValueMapping(target = "PROXY_MINUS_VOTE", source = "PROXY_VOTE")
    @ValueMapping(target = "OVERSEAS_MINUS_VOTE", source = "OVERSEAS_VOTE")
    @ValueMapping(target = "APPLICATIONS_MINUS_API", source = "APPLICATIONS_API")
    fun fromEntityToVcaSqsEnum(entitySourceType: SourceTypeEntityEnum): SourceTypeSqsEnum

    fun fromEntityToDtoEnum(entitySourceType: SourceTypeEntityEnum): SourceTypeDtoEnum

    @InheritInverseConfiguration
    fun fromDtoToEntityEnum(dtoSourceType: SourceTypeDtoEnum): SourceTypeEntityEnum

    @ValueMapping(target = "EROP", source = "VOTER_CARD")
    @ValueMapping(target = "EROP", source = "POSTAL_VOTE")
    @ValueMapping(target = "EROP", source = "PROXY_VOTE")
    @ValueMapping(target = "EROP", source = "OVERSEAS_VOTE")
    @ValueMapping(target = "EROP", source = "APPLICATIONS_API")
    fun sourceTypeDtoToSourceSystem(sourceType: SourceType): SourceSystem
}
