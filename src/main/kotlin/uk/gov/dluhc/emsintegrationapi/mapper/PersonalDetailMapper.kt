package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.InheritInverseConfiguration
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.dluhc.emsintegrationapi.database.entity.PersonalDetail
import uk.gov.dluhc.emsintegrationapi.dto.PersonalDetailDto

@Mapper
interface PersonalDetailMapper {

    @Mapping(target = "phoneNumber", source = "phone")
    fun personalDetailDtoToPersonalDetailEntity(personalDetailDto: PersonalDetailDto): PersonalDetail

    @InheritInverseConfiguration
    fun personalDetailEntityToPersonalDetailDto(personalDetail: PersonalDetail): PersonalDetailDto
}
