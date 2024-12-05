package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplicationPrimaryElectorDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.PrimaryElectorDetails

@Component
class PrimaryElectorDetailsMapper(private val addressMapper: AddressMapper) {
    fun mapToEntity(applicationId: String, primaryElectorDetails: PrimaryElectorDetails?) = primaryElectorDetails?.let {
        PostalVoteApplicationPrimaryElectorDetails(
            applicationId = applicationId,
            firstName = it.firstName,
            middleNames = it.middleNames,
            surname = it.surname,
            address = addressMapper.mapToAddressEntity(it.address, SourceSystem.POSTAL)!!,
        )
    }
}
