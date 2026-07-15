package uk.gov.dluhc.emsintegrationapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckMatchResultSentAtByGssCode
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.dto.AdminPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.models.AdminPendingRegisterCheckSummary
import uk.gov.dluhc.registercheckerapi.models.AdminPendingRegisterCheck

/**
 * Maps the entity class [RegisterCheck] to/from the corresponding [AdminPendingRegisterCheckDto].
 */
@Mapper(
    uses = [
        InstantMapper::class,
    ]
)
abstract class AdminPendingRegisterCheckMapper {

    @Mapping(target = "createdAt", source = "dateCreated")
    abstract fun registerCheckEntityToAdminPendingRegisterCheckDto(registerCheck: RegisterCheck): AdminPendingRegisterCheckDto

    @Mapping(target = "applicationId", source = "sourceReference")
    abstract fun adminPendingRegisterCheckDtoToAdminPendingRegisterCheckModel(pendingRegisterCheckDto: AdminPendingRegisterCheckDto): AdminPendingRegisterCheck

    @Mapping(target = "gssCode", source = "gssCode")
    @Mapping(target = "registerCheckCount", source = "pendingChecksSummary.registerCheckCount", defaultValue = "0")
    @Mapping(target = "earliestDateCreated", source = "pendingChecksSummary.earliestDateCreated")
    @Mapping(target = "lastSuccessfulRegisterCheck", source = "mostRecentResponse.latestMatchResultSentAt")
    abstract fun toAdminPendingRegisterCheckSummaryModel(
        gssCode: String,
        pendingChecksSummary: RegisterCheckSummaryByGssCode?,
        mostRecentResponse: RegisterCheckMatchResultSentAtByGssCode?,
    ): AdminPendingRegisterCheckSummary
}
