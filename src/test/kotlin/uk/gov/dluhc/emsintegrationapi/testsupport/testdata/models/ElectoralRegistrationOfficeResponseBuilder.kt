package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models

import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomEmailAddress
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomEroId
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomEroName
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomGssCode
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomLocalAuthorityName
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomPhoneNumber
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomWebsiteAddress
import uk.gov.dluhc.eromanagementapi.models.Address
import uk.gov.dluhc.eromanagementapi.models.ContactDetails
import uk.gov.dluhc.eromanagementapi.models.ElectoralRegistrationOfficeResponse
import uk.gov.dluhc.eromanagementapi.models.LocalAuthorityResponse

fun buildElectoralRegistrationOfficeResponse(
    eroId: String = getRandomEroId(),
    eroName: String = getRandomEroName(),
    localAuthorities: MutableList<LocalAuthorityResponse> = buildLocalAuthorityResponses(),
    roles: List<String>? = null
): ElectoralRegistrationOfficeResponse =
    ElectoralRegistrationOfficeResponse(
        id = eroId,
        name = eroName,
        localAuthorities = localAuthorities,
        roles = roles,
    )

fun buildLocalAuthorityResponses(): MutableList<LocalAuthorityResponse> {
    return mutableListOf(buildLocalAuthorityResponse())
}

fun buildLocalAuthorityResponse(
    gssCode: String = getRandomGssCode(),
    name: String = getRandomLocalAuthorityName(),
): LocalAuthorityResponse =
    LocalAuthorityResponse(
        gssCode = gssCode,
        name = name,
        contactDetailsEnglish = buildContactDetails(),
        contactDetailsWelsh = null,
    )

fun buildContactDetails(
    websiteAddress: String = getRandomWebsiteAddress(),
    phoneNumber: String = getRandomPhoneNumber(),
    emailAddress: String = getRandomEmailAddress(),
    address: Address = buildEroManagementAddress(),
): ContactDetails =
    ContactDetails(
        website = websiteAddress,
        phone = phoneNumber,
        email = emailAddress,
        address = address
    )

fun buildEroManagementAddress(
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
    property: String = faker.address().buildingNumber(),
    town: String = faker.address().city(),
    area: String = faker.address().state(),
): Address =
    Address(
        street = street,
        postcode = postcode,
        property = property,
        area = area,
        town = town
    )
