package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.annotation.CreatedBy
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.registercheckerapi.models.SourceSystem

fun buildAddress(
    street: String = faker.address().streetName(),
    property: String? = faker.address().buildingNumber(),
    locality: String? = faker.address().streetName(),
    town: String? = faker.address().city(),
    area: String? = faker.address().state(),
    postcode: String = faker.address().postcode(),
    uprn: String? = RandomStringUtils.randomNumeric(12),
    createdBy: SourceSystem? = null,
    version: Long? = null,
) = Address(
    street = street,
    property = property,
    locality = locality,
    town = town,
    area = area,
    postcode = postcode,
    uprn = uprn,
    createdBy = createdBy,
    version = version,
)

fun buildAddressWithOptionalFieldsAsNull(
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
) = buildAddress(
    street = street,
    postcode = postcode,
    property = null,
    locality = null,
    town = null,
    area = null,
    uprn = null,
    createdBy = null,
    version = null,
)
