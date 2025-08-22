package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging

import net.datafaker.Address
import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker
import uk.gov.dluhc.registercheckerapi.messaging.models.InitiateRegisterCheckMessage
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckAddress
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckPersonalDetail
import java.time.LocalDate
import java.util.UUID

fun buildInitiateRegisterCheckMessage(
    sourceReference: String = "VPIOKNHPBP",
    sourceCorrelationId: UUID = UUID.randomUUID(),
    requestedBy: String = "system",
    gssCode: String = "E123456789",
    personalDetail: RegisterCheckPersonalDetail = buildRegisterCheckPersonalDetail(),
    emsElectorId: String = RandomStringUtils.secure().nextAlphanumeric(30),
    historicalSearch: Boolean = false,
) = InitiateRegisterCheckMessage(
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    requestedBy = requestedBy,
    gssCode = gssCode,
    personalDetail = personalDetail,
    emsElectorId = emsElectorId,
    historicalSearch = historicalSearch,
)

private fun buildRegisterCheckPersonalDetail(
    firstName: String = DataFaker.faker.name().firstName(),
    middleNames: String? = DataFaker.faker.name().firstName(),
    surname: String = DataFaker.faker.name().lastName(),
    dateOfBirth: LocalDate? = DataFaker.faker.date().birthday().toLocalDateTime().toLocalDate(),
    email: String? = DataFaker.faker.internet().emailAddress(),
    phone: String? = DataFaker.faker.phoneNumber().cellPhone(),
    address: RegisterCheckAddress = buildRegisterCheckAddress()
) = RegisterCheckPersonalDetail(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    dateOfBirth = dateOfBirth,
    phone = phone,
    email = email,
    address = address
)

private fun buildRegisterCheckAddress(
    fakeAddress: Address = DataFaker.faker.address(),
    property: String? = fakeAddress.buildingNumber(),
    street: String = fakeAddress.streetName(),
    locality: String? = fakeAddress.streetName(),
    town: String? = fakeAddress.city(),
    area: String? = fakeAddress.state(),
    postcode: String = fakeAddress.postcode(),
    uprn: String? = RandomStringUtils.secure().nextNumeric(12),
) = RegisterCheckAddress(
    property = property,
    street = street,
    locality = locality,
    town = town,
    area = area,
    postcode = postcode,
    uprn = uprn,
)
