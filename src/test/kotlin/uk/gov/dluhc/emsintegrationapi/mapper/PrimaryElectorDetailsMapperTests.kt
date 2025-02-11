package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplicationPrimaryElectorDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPrimaryElectorDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomAlphaNumeric
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull
import uk.gov.dluhc.emsintegrationapi.messaging.models.PrimaryElectorDetails as PrimaryElectorDetailsMessageDto

internal class PrimaryElectorDetailsMapperTests {

    private val primaryElectorDetailsMapper = PrimaryElectorDetailsMapper(AddressMapper())

    @Nested
    inner class FromPrimaryElectorDetailsDtoToEntity {
        @Test
        fun `should convert primary elector details message dto to entity`() {
            validateMappedObject(
                ::buildPrimaryElectorDetailsMessageDto,
                { primaryElectorDetailsMapper.mapToEntity(getRandomAlphaNumeric(24), it) },
            )
        }

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull<PrimaryElectorDetailsMessageDto, PostalVoteApplicationPrimaryElectorDetails?> {
                primaryElectorDetailsMapper.mapToEntity(getRandomAlphaNumeric(24), it)
            }
    }
}
