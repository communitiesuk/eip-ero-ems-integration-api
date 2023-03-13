package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.util.UUID

fun buildAddressEntity(
    id: UUID? = UUID.randomUUID(),
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
    property: String = faker.address().buildingNumber(),
    locality: String = faker.address().streetName(),
    town: String = faker.address().city(),
    area: String = faker.address().state(),
    uprn: String = RandomStringUtils.randomNumeric(12),
    createdBy: SourceSystem? = SourceSystem.EMS
) = Address(
    id = id,
    street = street,
    postcode = postcode,
    property = property,
    locality = locality,
    town = town,
    area = area,
    uprn = uprn,
    createdBy = createdBy!!,
)
