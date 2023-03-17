package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails as ApplicantDetailsEntity

@Component
class ApplicantDetailsMapper(private val addressMapper: AddressMapper) {
    fun mapToApplicantEntity(applicantDetails: ApplicantDetails, createdBy: SourceSystem) = applicantDetails.let {
        ApplicantDetailsEntity(
            firstName = it.firstName,
            middleNames = it.middleNames,
            surname = it.surname,
            email = it.email,
            dob = it.dob,
            phone = it.phone,
            referenceNumber = it.referenceNumber,
            ipAddress = it.ipAddress,
            language = it.language,
            emsElectorId = it.emsElectorId,
            registeredAddress = addressMapper.mapToAddressEntity(it.registeredAddress, createdBy)!!
        )
    }
}
