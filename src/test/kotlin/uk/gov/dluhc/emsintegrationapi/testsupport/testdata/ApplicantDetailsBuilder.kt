package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.LocalDate
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address as AddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails as ApplicantDetailsMessageDto

fun buildApplicantDetailsEntity(
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    email: String? = faker.internet().emailAddress(),
    dob: LocalDate? = getRandomDOB(),
    phone: String? = faker.phoneNumber().phoneNumber(),
    registeredAddress: Address? = buildAddressEntity(),
    referenceNumber: String? = RandomStringUtils.randomNumeric(10),
    ipAddress: String = getRandomIpAddress(),
    language: ApplicantDetails.Language = ApplicantDetails.Language.EN,
    emsElectorId: String? = RandomStringUtils.randomNumeric(20)
) = ApplicantDetails(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    email = email,
    dob = dob,
    phone = phone,
    registeredAddress = registeredAddress!!,
    referenceNumber = referenceNumber!!,
    ipAddress = ipAddress,
    language = language,
    emsElectorId = emsElectorId
)

fun buildApplicantDetailsMessageDto(
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    email: String? = faker.internet().emailAddress(),
    dob: LocalDate? = getRandomDOB(),
    phone: String? = faker.phoneNumber().phoneNumber(),
    registeredAddress: AddressMessageDto? = buildAddressMessageDto(),
    referenceNumber: String = RandomStringUtils.randomNumeric(10),
    ipAddress: String = getRandomIpAddress(),
    language: ApplicantDetailsMessageDto.Language = ApplicantDetailsMessageDto.Language.EN,
    emsElectorId: String? = RandomStringUtils.randomNumeric(20)
) = ApplicantDetailsMessageDto(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    email = email,
    dob = dob,
    phone = phone,
    registeredAddress = registeredAddress!!,
    referenceNumber = referenceNumber,
    ipAddress = ipAddress,
    language = language,
    emsElectorId = emsElectorId
)

fun buildMinimalApplicantDetailsMessageDto() = buildApplicantDetailsMessageDto(
    middleNames = null,
    email = null,
    dob = null,
    phone = null,
    emsElectorId = null
)
