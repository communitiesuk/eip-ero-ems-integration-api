package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.registercheckerapi.models.SourceSystem
import java.time.Instant
import java.util.UUID
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem as SourceSystemEntity

fun buildAddress(
    persisted: Boolean = false,
    street: String = faker.address().streetName(),
    property: String? = faker.address().buildingNumber(),
    locality: String? = faker.address().streetName(),
    town: String? = faker.address().city(),
    area: String? = faker.address().state(),
    postcode: String = faker.address().postcode(),
    uprn: String? = RandomStringUtils.randomNumeric(12),
    dateCreated: Instant? = Instant.now(),
    createdBy: SourceSystem? = null
) = Address(
    id = if (persisted) UUID.randomUUID() else null,
    street = street,
    property = property,
    locality = locality,
    town = town,
    area = area,
    postcode = postcode,
    uprn = uprn,
    dateCreated = dateCreated,
    createdBy = createdBy?.let { SourceSystemEntity.valueOf(it.toString()) }
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
    dateCreated = Instant.now(),
    createdBy = null
)
