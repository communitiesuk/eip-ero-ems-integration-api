package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address as AddressMessageDto

fun buildAddressEntity(
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
    property: String = faker.address().buildingNumber(),
    locality: String = faker.address().streetName(),
    town: String = faker.address().city(),
    area: String = faker.address().state(),
    uprn: String = RandomStringUtils.randomNumeric(12),
    dateCreated: Instant = Instant.now(),
    createdBy: SourceSystem? = SourceSystem.EROP
) = Address(
    street = street,
    postcode = postcode,
    property = property,
    locality = locality,
    town = town,
    area = area,
    uprn = uprn,
    dateCreated = dateCreated,
    createdBy = createdBy!!,
)

fun buildAddressMessageDto(
    street: String = faker.address().streetName(),
    postcode: String = faker.address().postcode(),
    property: String = faker.address().buildingNumber(),
    locality: String = faker.address().streetName(),
    town: String = faker.address().city(),
    area: String = faker.address().state(),
    uprn: String = RandomStringUtils.randomNumeric(12),
) = AddressMessageDto(
    street = street,
    postcode = postcode,
    property = property,
    locality = locality,
    town = town,
    area = area,
    uprn = uprn,
)
