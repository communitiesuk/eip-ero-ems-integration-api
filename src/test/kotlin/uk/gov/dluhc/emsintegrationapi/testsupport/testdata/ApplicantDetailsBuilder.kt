package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.LocalDate

fun buildApplicantDetailsEntity(
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    email: String? = faker.internet().emailAddress(),
    dob: LocalDate? = getRandomDOB(),
    phoneNumber: String? = faker.phoneNumber().phoneNumber(),
    registeredAddress: Address? = buildAddressEntity(),
    referenceNumber: String? = RandomStringUtils.randomNumeric(10),
    ipAddress: String = getRandomIpAddress(),
    language: String = faker.locale.language,
    emsElectoralId: String = RandomStringUtils.randomNumeric(20)
) = ApplicantDetails(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    email = email,
    dob = dob,
    phoneNumber = phoneNumber,
    registeredAddress = registeredAddress!!,
    referenceNumber = referenceNumber!!,
    ipAddress = ipAddress,
    language = language,
    emsElectoralId = emsElectoralId
)
