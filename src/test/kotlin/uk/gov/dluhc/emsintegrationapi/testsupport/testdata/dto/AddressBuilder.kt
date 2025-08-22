package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.dto.AddressDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker

fun buildAddressDto(
    street: String = faker.address().streetName(),
    property: String? = faker.address().buildingNumber(),
    locality: String? = faker.address().streetName(),
    town: String? = faker.address().city(),
    area: String? = faker.address().state(),
    postcode: String = faker.address().postcode(),
    uprn: String? = RandomStringUtils.secure().nextNumeric(12),
    createdBy: String? = null,
) = AddressDto(
    street = street,
    property = property,
    locality = locality,
    town = town,
    area = area,
    postcode = postcode,
    uprn = uprn,
    createdBy = createdBy,
)

fun buildAddressDtoWithOptionalFieldsNull(
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
) = buildAddressDto(
    street = street,
    postcode = postcode,
    property = null,
    locality = null,
    town = null,
    area = null,
    uprn = null,
    createdBy = null,
)
