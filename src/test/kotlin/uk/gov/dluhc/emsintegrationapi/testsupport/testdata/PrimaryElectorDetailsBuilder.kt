package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplicationPrimaryElectorDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address as AddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.PrimaryElectorDetails as PrimaryElectorDetailsMessageDto

fun buildPrimaryElectorDetailsEntity(
    applicationId: String,
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    address: Address = buildAddressEntity(),
) = PostalVoteApplicationPrimaryElectorDetails(
    applicationId = applicationId,
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    address = address,
)

fun buildPrimaryElectorDetailsMessageDto(
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    address: AddressMessageDto = buildAddressMessageDto(),
) = PrimaryElectorDetailsMessageDto(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    address = address,
)
