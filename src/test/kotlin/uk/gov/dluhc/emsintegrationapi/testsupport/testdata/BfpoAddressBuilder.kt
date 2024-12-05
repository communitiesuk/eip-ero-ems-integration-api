package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.BfpoAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.BfpoAddress as BfpoAddressMessageDto

fun buildBfpoAddressEntity(
    bfpoNumber: String = RandomStringUtils.randomNumeric(7),
    addressLine1: String = faker.address().streetName(),
    addressLine2: String = faker.address().postcode(),
    addressLine3: String = faker.address().buildingNumber(),
    addressLine4: String = faker.address().streetName(),
    addressLine5: String = faker.address().city(),
    dateCreated: Instant = Instant.now(),
    createdBy: SourceSystem? = SourceSystem.EROP
) = BfpoAddress(
    bfpoNumber = bfpoNumber,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    addressLine5 = addressLine5,
    dateCreated = dateCreated,
    createdBy = createdBy!!
)

fun buildBfpoAddressMessageDto(
    bfpoNumebr: String = RandomStringUtils.randomNumeric(7),
    addressLine1: String = faker.address().streetName(),
    addressLine2: String = faker.address().postcode(),
    addressLine3: String = faker.address().buildingNumber(),
    addressLine4: String = faker.address().streetName(),
    addressLine5: String = faker.address().city(),
) = BfpoAddressMessageDto(
    bfpoNumber = bfpoNumebr,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    addressLine5 = addressLine5,
)
