package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.Address as AddressEntity

@Component
class AddressMapper {
    fun mapToAddressEntity(address: Address?) = address?.let {
        AddressEntity(
            street = it.street,
            property = it.property,
            locality = it.locality,
            town = it.town,
            area = it.area,
            postcode = it.postcode,
            uprn = it.uprn,
            createdBy = SourceSystem.POSTAL
        )
    }
}
