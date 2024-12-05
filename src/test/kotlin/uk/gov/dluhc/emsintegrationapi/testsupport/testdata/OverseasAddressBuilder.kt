package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.OverseasAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.OverseasAddress as OverseasAddressMessageDto

fun buildOverseasAddressEntity(
    addressLine1: String = faker.address().streetName(),
    addressLine2: String = faker.address().postcode(),
    addressLine3: String = faker.address().buildingNumber(),
    addressLine4: String = faker.address().streetName(),
    addressLine5: String = faker.address().city(),
    country: String = faker.address().country(),
    dateCreated: Instant = Instant.now(),
    createdBy: SourceSystem? = SourceSystem.EROP
) = OverseasAddress(
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    addressLine5 = addressLine5,
    country = country,
    dateCreated = dateCreated,
    createdBy = createdBy!!
)

fun buildOverseasAddressMessageDto(
    addressLine1: String = faker.address().streetName(),
    addressLine2: String = faker.address().postcode(),
    addressLine3: String = faker.address().buildingNumber(),
    addressLine4: String = faker.address().streetName(),
    addressLine5: String = faker.address().city(),
    country: String = faker.address().country(),
) = OverseasAddressMessageDto(
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    addressLine3 = addressLine3,
    addressLine4 = addressLine4,
    addressLine5 = addressLine5,
    country = country,
)
